package mobi.stos.podataka_lib.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import org.reflections.ReflectionUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mobi.stos.podataka_lib.annotations.Column;
import mobi.stos.podataka_lib.annotations.ForeignKey;
import mobi.stos.podataka_lib.annotations.PrimaryKey;
import mobi.stos.podataka_lib.annotations.Transient;
import mobi.stos.podataka_lib.connection.SQLiteConnect;
import mobi.stos.podataka_lib.exception.NoPrimaryKeyFoundException;
import mobi.stos.podataka_lib.exception.NoPrimaryKeyValueFoundException;
import mobi.stos.podataka_lib.interfaces.IOperations;
import mobi.stos.podataka_lib.interfaces.ISQLHelper;
import mobi.stos.podataka_lib.reflection.PropertyUtils;

public abstract class AbstractRepository<T extends Serializable> implements IOperations<T>, ISQLHelper<T> {

    public Class<T> klass;
    private Context context;
    private SQLiteDatabase sqLiteDatabase;
    private Cursor cursor;

    public enum MODE {
        READABLE,
        WRITABLE
    }

    public AbstractRepository(Context context, Class<T> klass) {
        this.context = context;
        this.klass = klass;
    }

    @Override
    public String table() {
        return klass.getSimpleName();
    }

    private String getPrimaryKey() throws NoPrimaryKeyFoundException {
        for (Field field : klass.getDeclaredFields()) {
            boolean isPrimaryKey = field.isAnnotationPresent(PrimaryKey.class);
            boolean isTransient = field.isAnnotationPresent(Transient.class);
            if (!isTransient && isPrimaryKey) {
                String name = field.getName().toLowerCase();
                PrimaryKey pk = field.getAnnotation(PrimaryKey.class);
                if (!TextUtils.isEmpty(pk.name())) {
                    name = pk.name();
                }
                return name;
            }
        }
        throw new NoPrimaryKeyFoundException();
    }

    private Object getPrimaryValue(T entity) throws NoPrimaryKeyValueFoundException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        for (Field field : klass.getDeclaredFields()) {
            boolean isPrimaryKey = field.isAnnotationPresent(PrimaryKey.class);
            boolean isTransient = field.isAnnotationPresent(Transient.class);
            if (!isTransient && isPrimaryKey) {
                Object value = PropertyUtils.getProperty(entity, field.getName().toLowerCase());
                if (value == null) {
                    throw new NoPrimaryKeyValueFoundException();
                } else {
                    Type type = field.getGenericType();
                    if (type instanceof Class && ((Class<?>) type).isEnum()) {
                        return ((Enum) value).ordinal();
                    } else  if (value instanceof Date) {
                        return ((Date) value).getTime();
                    } else if (value instanceof Boolean) {
                        return ((Boolean) value) ? 1 : 0;
                    } else if (value instanceof Integer) {
                        return (Integer) value;
                    } else if (value instanceof Double) {
                        return (Double) value;
                    } else if (value instanceof Long) {
                        return (Long) value;
                    } else if (value instanceof Float) {
                        return (Float) value;
                    } else if (value instanceof Short) {
                        return (Short) value;
                    } else {
                        return String.valueOf(value);
                    }
                }
            }
        }
        throw new NoPrimaryKeyValueFoundException();
    }

    private T getCursor() {
        String logName = "";
        try {
            T entity = klass.newInstance();
            for (Field field : klass.getDeclaredFields()) {
                boolean isPrimaryKey = field.isAnnotationPresent(PrimaryKey.class);
                boolean isForeingKey = field.isAnnotationPresent(ForeignKey.class);
                boolean isColumn = field.isAnnotationPresent(Column.class);
                boolean isTransient = field.isAnnotationPresent(Transient.class);
                if (!isTransient) {

                    String name = field.getName().toLowerCase();
                    String columnName = name;
                    logName = name;

                    if (isPrimaryKey) {
                        PrimaryKey pk = field.getAnnotation(PrimaryKey.class);
                        if (!TextUtils.isEmpty(pk.name())) {
                            name = pk.name();
                        }
                        PropertyUtils.setProperty(entity, name, getInt(name));
                        continue;
                    } else if (isForeingKey) {
                        ForeignKey fk = field.getAnnotation(ForeignKey.class);

                        String reference = field.getType().getSimpleName();
                        String referenceField = "";
                        Set<Field> fkey = ReflectionUtils.getAllFields(field.getType(), ReflectionUtils.withAnnotation(PrimaryKey.class));
                        for (Field f : fkey) {
                            referenceField = f.getName();
                            PrimaryKey pk = f.getAnnotation(PrimaryKey.class);
                            if (!pk.name().equals("")) {
                                referenceField = pk.name();
                            }
                        }

                        referenceField = referenceField.toLowerCase();
                        reference = reference.toLowerCase() + "_" + referenceField;
                        if (!TextUtils.isEmpty(fk.name())) {
                            reference = fk.name();
                        }

                        Object fkObject = field.getType().newInstance();

                        PropertyUtils.setProperty(fkObject, referenceField, getInt(reference));
                        PropertyUtils.setProperty(entity, field.getName(), fkObject);
                        continue;
                    } else if (isColumn) {
                        Column c = field.getAnnotation(Column.class);
                        if (!TextUtils.isEmpty(c.name())) {
                            columnName = c.name();
                        }
                    }
                    Type type = field.getGenericType();
                    if (type instanceof Class && ((Class<?>) type).isEnum()) {
                        PropertyUtils.setProperty(entity, name, field.getType().getEnumConstants()[getInt(columnName)]);
                    } else if (type == Integer.TYPE) {
                        PropertyUtils.setProperty(entity, name, getInt(columnName));
                    } else if (type == Double.TYPE) {
                        PropertyUtils.setProperty(entity, name, getDouble(columnName));
                    } else if (type == Float.TYPE) {
                        PropertyUtils.setProperty(entity, name, getFloat(columnName));
                    } else if (type == Long.TYPE || type == Long.class) {
                        PropertyUtils.setProperty(entity, name, getLong(columnName));
                    } else if (type == Boolean.TYPE) {
                        PropertyUtils.setProperty(entity, name, getBoolean(columnName));
                    } else if (type == Date.class) {
                        if (getLong(name) != null) {
                            PropertyUtils.setProperty(entity, name, new Date(getLong(columnName)));
                        }
                    } else {
                        PropertyUtils.setProperty(entity, name, getString(columnName));
                    }
                }
            }
            return entity;
        } catch (Exception e) {
            Log.e(AbstractRepository.class.getSimpleName(), "Erro ao tentar obter informação do getCursor() : " + logName);
            e.printStackTrace();
            return null;
        }
    }

    private String[] columns() {
        Set<String> fields = new HashSet<>();
        for (Field field : klass.getDeclaredFields()) {
            boolean isPrimaryKey = field.isAnnotationPresent(PrimaryKey.class);
            boolean isForeingKey = field.isAnnotationPresent(ForeignKey.class);
            boolean isColumn = field.isAnnotationPresent(Column.class);
            boolean isTransient = field.isAnnotationPresent(Transient.class);
            if (!isTransient) {
                String name = field.getName().toLowerCase();

                if (isPrimaryKey) {
                    PrimaryKey pk = field.getAnnotation(PrimaryKey.class);
                    if (!TextUtils.isEmpty(pk.name())) {
                        name = pk.name();
                    }
                } else if (isForeingKey) {
                    ForeignKey fk = field.getAnnotation(ForeignKey.class);

                    String reference = field.getType().getSimpleName();
                    String referenceField = "";
                    Set<Field> fkey = ReflectionUtils.getAllFields(field.getType(), ReflectionUtils.withAnnotation(PrimaryKey.class));
                    for (Field f : fkey) {
                        referenceField = f.getName();
                        PrimaryKey pk = f.getAnnotation(PrimaryKey.class);
                        if (!pk.name().equals("")) {
                            referenceField = pk.name();
                        }
                    }

                    reference = reference.toLowerCase() + "_" + referenceField.toLowerCase();
                    if (!TextUtils.isEmpty(fk.name())) {
                        reference = fk.name();
                    }

                    name = reference;

                } else if (isColumn) {
                    Column c = field.getAnnotation(Column.class);
                    if (!TextUtils.isEmpty(c.name())) {
                        name = c.name();
                    }
                }

                fields.add(name.toLowerCase());
            }
        }

        int i = 0;
        String[] colunas = new String[fields.size()];
        for (String field : fields) {
            colunas[i] = field;
            i++;
        }
        return colunas;
    }

    private ContentValues getValues(T entity) {
        ContentValues values = new ContentValues();
        for (Field field : klass.getDeclaredFields()) {
            try {
                boolean isTransient = field.isAnnotationPresent(Transient.class);
                if (!isTransient) {
                    Object value = PropertyUtils.getProperty(entity, field.getName().toLowerCase());
                    if (value == null)
                        continue;

                    boolean isPrimaryKey = field.isAnnotationPresent(PrimaryKey.class);
                    boolean isForeingKey = field.isAnnotationPresent(ForeignKey.class);
                    boolean isColumn = field.isAnnotationPresent(Column.class);

                    String name = field.getName();

                    if (isPrimaryKey) {
                        PrimaryKey pk = field.getAnnotation(PrimaryKey.class);
                        if (pk.autoIncrement()) {
                            continue;
                        }
                        if (!TextUtils.isEmpty(pk.name())) {
                            name = pk.name();
                        }
                    } else if (isForeingKey) {
                        ForeignKey fk = field.getAnnotation(ForeignKey.class);

                        String reference = field.getType().getSimpleName();
                        String referenceField = "";
                        Set<Field> fkey = ReflectionUtils.getAllFields(field.getType(), ReflectionUtils.withAnnotation(PrimaryKey.class));
                        for (Field f : fkey) {
                            referenceField = f.getName();
                            PrimaryKey pk = f.getAnnotation(PrimaryKey.class);
                            if (!pk.name().equals("")) {
                                referenceField = pk.name();
                            }
                        }

                        reference = reference.toLowerCase() + "_" + referenceField.toLowerCase();
                        if (!TextUtils.isEmpty(fk.name())) {
                            reference = fk.name();
                        }

                        name = reference;
                        value = PropertyUtils.getProperty(value, referenceField);
                    } else if (isColumn) {
                        Column c = field.getAnnotation(Column.class);
                        if (!TextUtils.isEmpty(c.name())) {
                            name = c.name();
                        }
                    }

                    Type type = field.getGenericType();
                    if (type instanceof Class && ((Class<?>) type).isEnum()) {
                        values.put(name.toLowerCase(), ((Enum) value).ordinal());
                    } else  if (value instanceof Date) {
                        values.put(name.toLowerCase(), ((Date) value).getTime());
                    } else if (value instanceof Boolean) {
                        values.put(name.toLowerCase(), ((Boolean) value) ? 1 : 0);
                    } else if (value instanceof Integer) {
                        values.put(name.toLowerCase(), (Integer) value);
                    } else if (value instanceof Double) {
                        values.put(name.toLowerCase(), (Double) value);
                    } else if (value instanceof Long) {
                        values.put(name.toLowerCase(), (Long) value);
                    } else if (value instanceof Float) {
                        values.put(name.toLowerCase(), (Float) value);
                    } else if (value instanceof Short) {
                        values.put(name.toLowerCase(), (Short) value);
                    } else {
                        values.put(name.toLowerCase(), String.valueOf(value));
                    }
                }
            } catch (Exception e) {
                Log.e(AbstractRepository.class.getSimpleName(), "Erro ao tentar obter informação do getValues() : " + field.getName());
                e.printStackTrace();
            }
        }
        return values;
    }

    public void setKlass(Class<T> klass) {
        this.klass = klass;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public final SQLiteDatabase getSqLiteDatabase(MODE mode) {
        SQLiteConnect sqLiteConnect = new SQLiteConnect(context);
        switch (mode) {
            case READABLE:
                sqLiteDatabase = sqLiteConnect.getReadableDatabase();
                break;
            case WRITABLE:
                sqLiteDatabase = sqLiteConnect.getWritableDatabase();
                break;
        }
        sqLiteDatabase.beginTransaction();
        return sqLiteDatabase;
    }

    public final SQLiteDatabase getSqLiteDatabase() {
        return getSqLiteDatabase(MODE.WRITABLE);
    }

    public void commit() {
        if (sqLiteDatabase != null && sqLiteDatabase.isOpen())
            sqLiteDatabase.setTransactionSuccessful();
    }

    public void closeSQLiteDatabase() {
        if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
            sqLiteDatabase.endTransaction();
            sqLiteDatabase.close();
        }
    }

    protected int getInt(String columnName) {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }

    protected String getString(String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }

    protected Double getDouble(String columnName) {
        return cursor.getDouble(cursor.getColumnIndex(columnName));
    }

    protected Float getFloat(String columnName) {
        return cursor.getFloat(cursor.getColumnIndex(columnName));
    }

    protected Long getLong(String columnName) {
        return cursor.getLong(cursor.getColumnIndex(columnName));
    }

    protected Date getDate(String columnName) {
        return new Date(getLong(columnName));
    }

    protected boolean getBoolean(String columnName) {
        return getInt(columnName) == 1;
    }

    @Override
    public void insert(T entity) {
        try {
            getSqLiteDatabase().insert(table(), null, getValues(entity));
            commit();
        } finally {
            closeSQLiteDatabase();
        }
    }

    @Override
    public void insert(List<T> entities) {
        try {
            SQLiteDatabase sqLiteDatabase = getSqLiteDatabase();
            for (T entity : entities) {
                sqLiteDatabase.insertWithOnConflict(table(), null, getValues(entity), SQLiteDatabase.CONFLICT_REPLACE);
            }
            commit();
        } finally {
            closeSQLiteDatabase();
        }
    }

    /**
     * Use this code only if you have a simple primary key.
     * For complex primary key create your own method
     * @param entity @Entity
     */
    @Override
    public void update(T entity) throws NoPrimaryKeyFoundException, NoPrimaryKeyValueFoundException {
        try {
            getSqLiteDatabase().update(table(), getValues(entity), this.getPrimaryKey() + " = ?", new String[]{String.valueOf( getPrimaryValue(entity) )});
            commit();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            closeSQLiteDatabase();
        }
    }

    /**
     * Use this code only if you have a simple primary key.
     * For complex primary key create your own method
     * @param entity Entity
     */
    @Override
    public void delete(T entity) throws NoPrimaryKeyFoundException, NoPrimaryKeyValueFoundException {
        try {
            getSqLiteDatabase().delete(table(), this.getPrimaryKey() + " = ?", new String[]{String.valueOf( getPrimaryValue(entity) )});
            commit();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            closeSQLiteDatabase();
        }
    }

    @Override
    public List<T> list() {
        return list(null, null, null, -1);
    }

    @Override
    public List<T> list(int limit) {
        return list(null, null, null, limit);
    }

    @Override
    public List<T> list(String orderBy) {
        return list(null, null, orderBy, -1);
    }

    @Override
    public List<T> list(String fields, String[] values) {
        return list(fields, values, null, -1);
    }

    @Override
    public List<T> list(String fields, String[] values, String orderBy) {
        return list(fields, values, orderBy, -1);
    }

    @Override
    public List<T> list(String fields, String[] values, String orderBy, int limit) {
        try {
            List<T> entity = new ArrayList<>();
            String limite = limit == -1 ? null : String.valueOf(limit);
            cursor = getSqLiteDatabase(MODE.READABLE).query(table(), columns(), fields, values, null, null, orderBy, limite);
            while (cursor.moveToNext()) {
                entity.add(getCursor());
            }
            cursor.close();
            commit();
            return entity;
        } finally {
            if ( cursor != null && !cursor.isClosed())
                cursor.close();
            closeSQLiteDatabase();
        }
    }

    @Override
    public T get(String fields, String[] values) {
        try {
            T entity = null;
            cursor = getSqLiteDatabase(MODE.READABLE).query(table(), columns(), fields, values, null, null, null, "1");
            while (cursor.moveToNext()) {
                entity = getCursor();
            }
            cursor.close();
            commit();
            return entity;
        } finally {
            if ( cursor != null && !cursor.isClosed())
                cursor.close();
            closeSQLiteDatabase();
        }
    }

    @Override
    public int count(String fields, String[] values) {
        int contagem = 0;
        Cursor cursor = null;
        try {
            String sql = "SELECT COUNT(*) FROM " + table();
            if (!TextUtils.isEmpty(fields)) {
                sql += " WHERE " + fields ;
            }

            cursor = getSqLiteDatabase().rawQuery(sql, values);
            while (cursor.moveToNext()) {
                contagem = cursor.getInt(0);
            }
            cursor.close();
            commit();
            return contagem;
        } finally {
            if ( cursor != null && !cursor.isClosed())
                cursor.close();
            closeSQLiteDatabase();
        }
    }

    @Override
    public void clean() {
        try {
            getSqLiteDatabase().delete(table(), null, null);
            commit();
        } finally {
            closeSQLiteDatabase();
        }
    }
}
