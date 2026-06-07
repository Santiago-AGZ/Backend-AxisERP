-- ============================================================================
-- AUDITORÍA COMPLETA DE BASE DE DATOS POSTGRESQL
-- ============================================================================
-- Script para auditar tablas, columnas, constraints e índices

-- 1. LISTAR TODAS LAS TABLAS
SELECT
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
FROM pg_tables
WHERE schemaname NOT IN ('pg_catalog', 'information_schema')
ORDER BY schemaname, tablename;

-- 2. LISTAR COLUMNAS Y TIPOS PARA CADA TABLA
SELECT
    t.table_schema,
    t.table_name,
    c.column_name,
    c.data_type,
    c.character_maximum_length,
    c.numeric_precision,
    c.numeric_scale,
    c.is_nullable,
    c.column_default
FROM information_schema.tables t
JOIN information_schema.columns c ON t.table_name = c.table_name AND t.table_schema = c.table_schema
WHERE t.table_schema NOT IN ('pg_catalog', 'information_schema')
ORDER BY t.table_schema, t.table_name, c.ordinal_position;

-- 3. PRIMARY KEYS
SELECT
    t.table_schema,
    t.table_name,
    a.attname as column_name,
    i.relname as constraint_name
FROM pg_class t
JOIN pg_index ix ON t.oid = ix.indrelid
JOIN pg_class i ON i.oid = ix.indexrelid
JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = ANY(ix.indkey)
WHERE ix.indisprimary
    AND t.relnamespace != (SELECT oid FROM pg_namespace WHERE nspname = 'pg_catalog')
ORDER BY t.relname, a.attnum;

-- 4. FOREIGN KEYS
SELECT
    tc.table_schema,
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name,
    rc.constraint_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu ON tc.constraint_name = kcu.constraint_name AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu ON ccu.constraint_name = tc.constraint_name AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY'
ORDER BY tc.table_name, kcu.column_name;

-- 5. UNIQUE CONSTRAINTS
SELECT
    tc.table_schema,
    tc.table_name,
    kcu.column_name,
    tc.constraint_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu ON tc.constraint_name = kcu.constraint_name AND tc.table_schema = kcu.table_schema
WHERE tc.constraint_type = 'UNIQUE'
ORDER BY tc.table_name, kcu.column_name;

-- 6. NOT NULL CONSTRAINTS
SELECT
    table_schema,
    table_name,
    column_name,
    is_nullable
FROM information_schema.columns
WHERE table_schema NOT IN ('pg_catalog', 'information_schema')
    AND is_nullable = 'NO'
ORDER BY table_schema, table_name, column_name;

-- 7. CHECK CONSTRAINTS
SELECT
    constraint_schema,
    constraint_name,
    table_name,
    column_name,
    check_clause
FROM information_schema.check_constraints cc
JOIN information_schema.constraint_column_usage ccu ON cc.constraint_name = ccu.constraint_name
WHERE constraint_schema NOT IN ('pg_catalog', 'information_schema')
ORDER BY table_name, column_name;

-- 8. ÍNDICES
SELECT
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE schemaname NOT IN ('pg_catalog', 'information_schema')
ORDER BY schemaname, tablename, indexname;

-- 9. SECUENCIAS
SELECT
    sequence_schema,
    sequence_name,
    data_type,
    start_value,
    minimum_value,
    maximum_value,
    increment,
    cycle_option
FROM information_schema.sequences
WHERE sequence_schema NOT IN ('pg_catalog', 'information_schema')
ORDER BY sequence_schema, sequence_name;

-- 10. TABLAS CON VERSIONING/SOFT DELETE
SELECT
    table_schema,
    table_name,
    STRING_AGG(column_name, ', ' ORDER BY column_name) as timestamp_columns
FROM information_schema.columns
WHERE table_schema NOT IN ('pg_catalog', 'information_schema')
    AND (column_name IN ('created_at', 'updated_at', 'deleted_at', 'created_by', 'updated_by'))
GROUP BY table_schema, table_name
ORDER BY table_schema, table_name;

-- 11. TAMAÑO POR TABLA
SELECT
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as total_size,
    pg_size_pretty(pg_relation_size(schemaname||'.'||tablename)) as table_size,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename) - pg_relation_size(schemaname||'.'||tablename)) as indexes_size
FROM pg_tables
WHERE schemaname NOT IN ('pg_catalog', 'information_schema')
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- 12. EXTENSIONES INSTALADAS
SELECT
    extname,
    extversion,
    nspname as schema
FROM pg_extension
LEFT JOIN pg_namespace ON pg_namespace.oid = extnamespace
ORDER BY extname;

-- 13. RELACIONES TABLA-COLUMNA (para detectar columnas huérfanas)
SELECT
    t.table_schema,
    t.table_name,
    c.column_name,
    c.data_type,
    CASE
        WHEN c.column_name IN ('id', 'uuid') THEN 'PK/ID'
        WHEN c.column_name LIKE '%_id' THEN 'FK'
        WHEN c.column_name IN ('created_at', 'updated_at', 'deleted_at') THEN 'Audit'
        WHEN c.column_name IN ('created_by', 'updated_by') THEN 'Audit User'
        WHEN c.column_name IN ('version') THEN 'Versioning'
        ELSE 'Data'
    END as column_type
FROM information_schema.tables t
JOIN information_schema.columns c ON t.table_name = c.table_name AND t.table_schema = c.table_schema
WHERE t.table_schema NOT IN ('pg_catalog', 'information_schema')
ORDER BY t.table_schema, t.table_name, c.ordinal_position;
