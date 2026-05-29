import psycopg2

def check_table(conn_str, label, table):
    conn = psycopg2.connect(conn_str)
    cur = conn.cursor()
    cur.execute(
        "SELECT column_name, data_type FROM information_schema.columns "
        "WHERE table_name=%s ORDER BY ordinal_position", (table,)
    )
    cols = cur.fetchall()
    print(f"\nNEON {label} - tabla '{table}':")
    for c in cols:
        print(f"  {c[0]}: {c[1]}")
    conn.close()
    return [c[0] for c in cols]

# Sales - customers
sales_cols = check_table(
    "postgresql://neondb_owner:npg_qtvUFx6Ol8Ve@ep-misty-waterfall-apbais5g-pooler.c-7.us-east-1.aws.neon.tech:5432/neondb?sslmode=require",
    "SALES", "customers"
)
print("  -> 'codigo' presente:", "codigo" in sales_cols)

# Purchase - suppliers
purch_cols = check_table(
    "postgresql://neondb_owner:npg_xTB0s1gojVbU@ep-quiet-union-aqsh56i9-pooler.c-8.us-east-1.aws.neon.tech:5432/neondb?sslmode=require",
    "PURCHASE", "suppliers"
)
print("  -> 'codigo' presente:", "codigo" in purch_cols)

# Auth - profiles
auth_cols = check_table(
    "postgresql://neondb_owner:npg_6D4bJSLtAuzk@ep-wispy-voice-aqodv5c1-pooler.c-8.us-east-1.aws.neon.tech:5432/neondb?sslmode=require",
    "AUTH", "profiles"
)
print("  -> 'password_hash' presente:", "password_hash" in auth_cols)

# Inventory
inv_cols = check_table(
    "postgresql://neondb_owner:npg_4GmCpJk3bgqa@ep-still-resonance-apfxlmlm-pooler.c-7.us-east-1.aws.neon.tech:5432/neondb?sslmode=require",
    "INVENTORY", "inventory"
)
print("  -> 'updated_by' presente:", "updated_by" in inv_cols)
