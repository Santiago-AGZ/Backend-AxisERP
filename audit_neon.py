#!/usr/bin/env python3
"""
Auditoría de bases de datos en Neon
Valida qué tablas, esquemas y enums existen en cada BD
"""

import psycopg2
from urllib.parse import urlparse
import json

# Credenciales de Neon del .env
DATABASES = {
    "AUTH": "postgresql://neondb_owner:npg_6D4bJSLtAuzk@ep-wispy-voice-aqodv5c1-pooler.c-8.us-east-1.aws.neon.tech:5432/neondb?sslmode=require",
    "CATALOG": "postgresql://neondb_owner:npg_W9Zr4nodvQih@ep-rapid-night-aq5mpoqi-pooler.c-8.us-east-1.aws.neon.tech:5432/neondb?sslmode=require",
    "INVENTORY": "postgresql://neondb_owner:npg_4GmCpJk3bgqa@ep-still-resonance-apfxlmlm-pooler.c-7.us-east-1.aws.neon.tech:5432/neondb?sslmode=require",
    "SALES": "postgresql://neondb_owner:npg_qtvUFx6Ol8Ve@ep-misty-waterfall-apbais5g-pooler.c-7.us-east-1.aws.neon.tech:5432/neondb?sslmode=require",
    "PURCHASE": "postgresql://neondb_owner:npg_xTB0s1gojVbU@ep-quiet-union-aqsh56i9-pooler.c-8.us-east-1.aws.neon.tech:5432/neondb?sslmode=require",
    "REPORT": "postgresql://neondb_owner:npg_hnYTb4BIov5X@ep-flat-dream-aphtqln9-pooler.c-7.us-east-1.aws.neon.tech:5432/neondb?sslmode=require"
}

def connect_and_audit(service_name, connection_string):
    """Conectar a una BD y auditar su contenido"""
    try:
        conn = psycopg2.connect(connection_string)
        cur = conn.cursor()

        print(f"\n{'='*60}")
        print(f" {service_name} SERVICE AUDIT")
        print(f"{'='*60}\n")

        # 1. Listar tablas
        cur.execute("""
            SELECT table_name
            FROM information_schema.tables
            WHERE table_schema='public'
            ORDER BY table_name
        """)
        tables = cur.fetchall()

        print("TABLAS:")
        if tables:
            for (table_name,) in tables:
                # Obtener columnas de cada tabla
                cur.execute(f"""
                    SELECT column_name, data_type, is_nullable
                    FROM information_schema.columns
                    WHERE table_name='{table_name}'
                    ORDER BY ordinal_position
                """)
                columns = cur.fetchall()
                print(f"\n  {table_name}:")
                for col_name, col_type, nullable in columns:
                    nullable_str = "NULL" if nullable == "YES" else "NOT NULL"
                    print(f"    - {col_name}: {col_type} ({nullable_str})")
        else:
            print("  ❌ NO TABLES FOUND")

        # 2. Listar ENUMs
        cur.execute("""
            SELECT t.typname
            FROM pg_type t
            JOIN pg_namespace n ON n.oid = t.typnamespace
            WHERE n.nspname = 'public'
            AND t.typtype = 'e'
            ORDER BY t.typname
        """)
        enums = cur.fetchall()

        print(f"\n\nENUMS:")
        if enums:
            for (enum_name,) in enums:
                cur.execute(f"""
                    SELECT enumlabel
                    FROM pg_enum
                    JOIN pg_type ON pg_enum.enumtypid = pg_type.oid
                    WHERE pg_type.typname = '{enum_name}'
                    ORDER BY enumsortorder
                """)
                enum_values = cur.fetchall()
                print(f"\n  {enum_name}:")
                for (value,) in enum_values:
                    print(f"    - {value}")
        else:
            print("  ❌ NO ENUMS FOUND")

        # 3. Resumen
        print(f"\n\nRESUMEN:")
        print(f"  Total Tablas: {len(tables)}")
        print(f"  Total ENUMs: {len(enums)}")

        cur.close()
        conn.close()

        return {
            "status": "OK",
            "tables": len(tables),
            "enums": len(enums)
        }

    except Exception as e:
        print(f"\n{'='*60}")
        print(f" {service_name} SERVICE AUDIT - ERROR")
        print(f"{'='*60}")
        print(f"❌ {str(e)}")
        return {
            "status": "ERROR",
            "message": str(e)
        }

if __name__ == "__main__":
    print("\n" + "="*60)
    print(" NEON DATABASE AUDIT - AxisERP")
    print("="*60)

    results = {}
    for service_name, conn_str in DATABASES.items():
        results[service_name] = connect_and_audit(service_name, conn_str)

    # Resumen final
    print(f"\n\n{'='*60}")
    print(" AUDIT SUMMARY")
    print(f"{'='*60}\n")

    for service_name, result in results.items():
        if result["status"] == "OK":
            print(f"✅ {service_name}: {result['tables']} tables, {result['enums']} enums")
        else:
            print(f"❌ {service_name}: {result['message']}")
