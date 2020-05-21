package mobi.stos.podataka_lib.connection;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mobi.stos.podataka_lib.R;
import mobi.stos.podataka_lib.annotations.Column;
import mobi.stos.podataka_lib.annotations.Entity;
import mobi.stos.podataka_lib.annotations.ForeignKey;
import mobi.stos.podataka_lib.annotations.PrimaryKey;
import mobi.stos.podataka_lib.annotations.Transient;

public class SQLiteConnect extends SQLiteOpenHelper {

    private Context context;
    private final boolean DEBUG;
    private boolean safeDrop = false;

    private Map<String, Set<String>> tables = new HashMap<>();
    private Map<String, Set<String>> newTables = new HashMap<>();

    public SQLiteConnect(Context context) {
        super(context, context.getString(R.string.db_name), null, context.getResources().getInteger(R.integer.db_version));
        this.context = context;
        this.DEBUG = Boolean.parseBoolean(context.getString(R.string.db_log));
    }

    public Context getContext() {
        return context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        this.execCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        this.tables(db);
        this.execDrop(db);
        this.execCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        this.tables(db);
        this.execDrop(db);
        this.execCreate(db);
    }

    private void execCreate(SQLiteDatabase db) {
        String[] sqlite = this.create();
        for (String s : sqlite) {
            if (DEBUG) {
                Log.i("SQL", "exec: " + s);
            }
            db.execSQL(s);
        }
    }

    private void execDrop(SQLiteDatabase db) {
        if (safeDrop) {
            String[] sqlite = this.drops();
            for (String s : sqlite) {
                if (DEBUG) {
                    Log.i("SQL", "exec: " + s);
                }
                db.execSQL(s);
            }
        }
    }

    private Set<Class<?>> entities() {
        try {
            String[] scanners = getContext().getResources().getStringArray(R.array.db_scan);

            Set<Class<?>> classes = new HashSet<>();
            for (String scan : scanners) {
                Class c = Class.forName(scan);
                if (c.isAnnotationPresent(Entity.class)) {
                    classes.add(c);
                }
            }
            return classes;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /***
     * Function to drop all candidate tables on onUpgrade call event
     * Usar somente em caso de falha na consulta do esquema das tabelas.
     * @return String[]
     */
    @Deprecated
    private String[] drops() {
        Set<Class<?>> entities = entities();
        StringBuilder builder = new StringBuilder();
        for (Class<?> klass : entities) {
            Entity entity = klass.getAnnotation(Entity.class);
            if (entity.dropOnVersionUpdate()) {
                String table = klass.getSimpleName();
                if (!entity.name().equals("")) {
                    table = entity.name();
                }
                table = table.toLowerCase();
                builder.append("DROP TABLE IF EXISTS ").append(table).append(";");
            }
        }
        return builder.toString().split(";");
    }

    /***
     * Function create all candidate tables with attributes
     * @return String[]
     */
    private String[] create() {
        Set<Class<?>> entities = entities();
        StringBuilder builder = new StringBuilder();
        for (Class<?> klass : entities) {
            Entity entity = klass.getAnnotation(Entity.class);
            String table = klass.getSimpleName();
            if (!entity.name().equals("")) {
                table = entity.name();
            }
            table = table.toLowerCase();

            if (this.tables.size() == 0 || !this.tables.containsKey(table)) {
                builder.append(this.create(table, klass));
            } else {
                builder.append(this.alter(table, klass));
            }
        }
        return builder.toString().split(";");
    }

    private String create(String table, Class<?> klass) {

        Set<String> set = new HashSet<>();

        StringBuilder builder = new StringBuilder();

        StringBuilder sqlForeignKey = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS ").append(table).append(" (");
        boolean newTable = true;
        for (Field field : klass.getDeclaredFields()) {
            boolean isPrimaryKey = field.isAnnotationPresent(PrimaryKey.class);
            boolean isForeingKey = field.isAnnotationPresent(ForeignKey.class);
            boolean isColumn = field.isAnnotationPresent(Column.class);
            boolean isTransient = field.isAnnotationPresent(Transient.class);
            if (!isTransient && !field.isSynthetic()) {
                String name = field.getName().toLowerCase();
                if (name.equalsIgnoreCase("serialversionuid")) {
                    continue;
                }

                int length = 0;
                boolean nullable = true;

                if (!newTable) {
                    builder.append(",");
                }
                newTable = false;

                String forcedType = "";
                if (isPrimaryKey) {
                    PrimaryKey pk = field.getAnnotation(PrimaryKey.class);
                    if (!pk.name().equals("")) {
                        name = pk.name().toLowerCase();
                    }

                    builder.append(name).append(" ").append(sqlDataType(field)).append(" NOT NULL PRIMARY KEY");
                    if (pk.autoIncrement()) {
                        builder.append(" AUTOINCREMENT");
                    }

                    set.add(name);  // setando nome das colunas para auxiliar nos update de tabelas
                    continue;
                } else if (isForeingKey) {
                    ForeignKey fk = field.getAnnotation(ForeignKey.class);
                    if (!fk.name().equals("")) {
                        name = fk.name().toLowerCase();
                    }

                    String reference = field.getType().getSimpleName();
                    Entity fke = field.getType().getAnnotation(Entity.class);
                    if (!fke.name().equals("")) {
                        reference = fke.name();
                    }

                    Field fieldKey = null;
                    String referenceField = "";
                    for (Field f : field.getType().getDeclaredFields()) {
                        if (f.isAnnotationPresent(PrimaryKey.class)) {
                            referenceField = f.getName();
                            PrimaryKey pk = f.getAnnotation(PrimaryKey.class);
                            if (!pk.name().equals("")) {
                                referenceField = pk.name();
                            }
                            fieldKey = f;
                        }
                    }

                    referenceField = referenceField.toLowerCase();
                    reference = reference.toLowerCase() + "_" + referenceField;

                    builder.append(reference).append(" ").append(sqlDataType(fieldKey));
                    if (!fk.nullable()) {
                        builder.append(" NOT NULL");
                    }

                    if (!sqlForeignKey.toString().equals("")) {
                        sqlForeignKey.append(",");
                    }
                    sqlForeignKey.append("FOREIGN KEY (").append(reference).append(") ").append("REFERENCES ").append(name).append("(").append(referenceField).append(")");

                    set.add(reference);  // setando nome das colunas para auxiliar nos update de tabelas
                    continue;
                } else if (isColumn) {
                    Column column = field.getAnnotation(Column.class);
                    if (!column.name().equals("")) {
                        name = column.name().toLowerCase();
                    }
                    length = column.length();
                    forcedType = column.sqlType();
                    nullable = column.nullable();
                }

                if (forcedType.equals("")) {
                    builder.append(name).append(" ").append(sqlDataType(field, length));
                } else {
                    builder.append(name).append(" ").append(forcedType);
                }
                if (!nullable) {
                    builder.append(" NOT NULL");
                }

                set.add(name); // setando nome das colunas para auxiliar nos update de tabelas
            }
        }
        if (!sqlForeignKey.toString().equals("")) {
            builder.append(",").append(sqlForeignKey.toString());
        }
        builder.append(");");

        this.newTables.put(table, set);

        return builder.toString();
    }

    private String alter(String table, Class<?> klass) {
        StringBuilder builder = new StringBuilder();
        builder.append("PRAGMA foreign_keys = 0;");
        builder.append("CREATE TABLE sqlitestudio_temp_table_").append(table).append(" AS SELECT * FROM ").append(table).append(";");
        builder.append("DROP TABLE ").append(table).append(";");
        builder.append(this.create(table, klass));

        StringBuilder columnBuilder = new StringBuilder();
        Set<String> columns = this.tables.get(table);
        for (String column : columns) {
            boolean contains = false;
            for (String older : this.newTables.get(table)) {
                if (column.equalsIgnoreCase(older)) {
                    contains = true;
                    break;
                }
            }
            if (contains) {
                if (columnBuilder.length() > 0) {
                    columnBuilder.append(", ");
                }
                columnBuilder.append(column);
            }
        }

        builder.append("INSERT INTO ").append(table).append(" (").append(columnBuilder.toString()).append(") ")
                .append("SELECT ")
                .append(columnBuilder.toString())
                .append(" FROM sqlitestudio_temp_table_").append(table).append(";");

        builder.append("DROP TABLE sqlitestudio_temp_table_").append(table).append(";");
        builder.append("PRAGMA foreign_keys = 1;");

        return builder.toString();
    }

    private String sqlDataType(Field field) {
        return this.sqlDataType(field, -1);
    }

    private String sqlDataType(Field field, int length) {
        Type type = field.getGenericType();
        if ((type instanceof Class && ((Class<?>) type).isEnum()) || type == Integer.TYPE || type == Boolean.TYPE) {
            return "INTEGER";
        } else if (type == Double.TYPE) {
            return "DOUBLE PRECISION";
        } else if (type == Float.TYPE) {
            return "FLOAT";
        } else if (type == Long.TYPE || type == Long.class || type == Date.class) {
            return "LONG";
        } else {
            if (length > 0) {
                return "VARCHAR(" + length + ")";
            } else {
                return "TEXT";
            }
        }
    }

    private void tables(SQLiteDatabase db) {
        try {
            Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%'", null);
            while (cursor.moveToNext()) {
                String tableName = cursor.getString(cursor.getColumnIndex("name"));
                if (!TextUtils.isEmpty(tableName)) {
                    tableName = tableName.toLowerCase();

                    Set<String> columns = new HashSet<>();
                    Cursor tableCursor = db.rawQuery("PRAGMA TABLE_INFO('" + tableName + "')", null);
                    while (tableCursor.moveToNext()) {
                        columns.add(tableCursor.getString(tableCursor.getColumnIndex("name")));
                    }
                    if (!tableCursor.isClosed()) {
                        tableCursor.close();
                    }
                    this.tables.put(tableName, columns);
                }
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        } catch (Exception e) {
            safeDrop = true;
            if (DEBUG) {
                Log.e("SQL", "Erro ao tentar carregar as tabelas existentes. Erro: " + e.getMessage());
            }
        }
    }
}
