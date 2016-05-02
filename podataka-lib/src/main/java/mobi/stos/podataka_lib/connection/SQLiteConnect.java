package mobi.stos.podataka_lib.connection;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import mobi.stos.podataka_lib.R;
import mobi.stos.podataka_lib.annotations.Column;
import mobi.stos.podataka_lib.annotations.Entity;
import mobi.stos.podataka_lib.annotations.ForeignKey;
import mobi.stos.podataka_lib.annotations.PrimaryKey;
import mobi.stos.podataka_lib.annotations.Transient;
import mobi.stos.podataka_lib.exception.ForeignKeyCandidateException;
import mobi.stos.podataka_lib.exception.PrimaryKeyCandidateException;

public class SQLiteConnect extends SQLiteOpenHelper {

    private Context context;
    private final boolean DEBUG;

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
        String[] sqlite = this.create();
        for (String s : sqlite) {
            if (DEBUG) {
                Log.i("SQL", "exec: " + s);
            }
            db.execSQL(s);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String[] sqlite = this.drops();
        for (String s : sqlite) {
            if (DEBUG) {
                Log.i("SQL", "exec: " + s);
            }
            db.execSQL(s);
        }
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String[] sqlite = this.drops();
        for (String s : sqlite) {
            if (DEBUG) {
                Log.i("SQL", "exec: " + s);
            }
            db.execSQL(s);
        }
        onCreate(db);
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
     * @return String[]
     */
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
                    int length = 0;
                    boolean nullable = true;
                    //Log.v(this.getClass().getSimpleName(), "column: " + name);

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

                        if (!sqlDataType(field, 0).equals("INTEGER")) {
                            throw new PrimaryKeyCandidateException();
                        }

                        builder.append(name).append(" ").append(sqlDataType(field, 0)).append(" NOT NULL PRIMARY KEY");
                        if (pk.autoIncrement()) {
                            builder.append(" AUTOINCREMENT");
                        }
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

                        if (!sqlDataType(fieldKey, 0).equals("INTEGER")) {
                            throw new ForeignKeyCandidateException();
                        }

                        builder.append(reference).append(" ").append(sqlDataType(fieldKey, 0));
                        if (!fk.nullable()) {
                            builder.append(" NOT NULL");
                        }

                        if (!sqlForeignKey.toString().equals("")) {
                            sqlForeignKey.append(",");
                        }
                        sqlForeignKey.append("FOREIGN KEY (").append(reference).append(") ")
                                .append("REFERENCES ").append(name).append("(").append(referenceField).append(")");
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
                }
            }
            if (!sqlForeignKey.toString().equals("")) {
                builder.append(",").append(sqlForeignKey.toString());
            }
            builder.append(");");
        }
        return builder.toString().split(";");
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
            return "VARCHAR(" + length + ")";
        }
    }
}
