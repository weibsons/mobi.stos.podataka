package mobi.stos.podataka_lib.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

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

    public PropertyUtils propertyUtils;

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

        try {
            propertyUtils = new PropertyUtils(klass.newInstance());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String table() {
        return klass.getSimpleName();
    }

    private String getPrimaryKey() throws NoPrimaryKeyFoundException {
        for (Field field : klass.getDeclaredFields()) {
            boolean isPrimaryKey = field.isAnnotationPresent(PrimaryKey.class);
            boolean isTransient = field.isAnnotationPresent(Transient.class);
            if (!isTransient && isPrimaryKey && !field.isSynthetic()) {
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

    private Object getPrimaryValue() throws NoPrimaryKeyValueFoundException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        for (Field field : klass.getDeclaredFields()) {
            boolean isPrimaryKey = field.isAnnotationPresent(PrimaryKey.class);
            boolean isTransient = field.isAnnotationPresent(Transient.class);
            if (!isTransient && isPrimaryKey && !field.isSynthetic()) {
                if (!propertyUtils.exists(field.getName())) {
                    continue;
                }

                Object value = propertyUtils.getProperty(field.getName());
                if (value == null) {
                    throw new NoPrimaryKeyValueFoundException();
                } else {
                    Type type = field.getGenericType();
                    if (type instanceof Class && ((Class<?>) type).isEnum()) {
                        return ((Enum) value).ordinal();
                    } else if (value instanceof Date) {
                        return ((Date) value).getTime();
                    } else if (value instanceof Boolean) {
                        return ((Boolean) value) ? 1 : 0;
                    } else if (value instanceof Integer || value instanceof Double || value instanceof Long || value instanceof Float || value instanceof Short) {
                        return value;
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
            this.propertyUtils = new PropertyUtils(entity);
            for (Field field : klass.getDeclaredFields()) {
                boolean isPrimaryKey = field.isAnnotationPresent(PrimaryKey.class);
                boolean isForeingKey = field.isAnnotationPresent(ForeignKey.class);
                boolean isColumn = field.isAnnotationPresent(Column.class);
                boolean isTransient = field.isAnnotationPresent(Transient.class);
                if (!isTransient && !field.isSynthetic()) {
                    String name = field.getName().toLowerCase();
                    String columnName = name;
                    logName = name;


                    if (!propertyUtils.exists(name)) {
                        continue;
                    }

                    if (isPrimaryKey) {
                        PrimaryKey pk = field.getAnnotation(PrimaryKey.class);
                        if (!TextUtils.isEmpty(pk.name())) {
                            columnName = pk.name();
                        }
                        this._setValue(propertyUtils, field, name, columnName);
                        continue;
                    } else if (isForeingKey) {
                        ForeignKey fk = field.getAnnotation(ForeignKey.class);

                        String reference = field.getType().getSimpleName();
                        String referenceField = "";
                        Field fkField = null;
                        for (Field f : field.getType().getDeclaredFields()) {
                            if (f.isAnnotationPresent(PrimaryKey.class)) {
                                referenceField = f.getName();
                                PrimaryKey pk = f.getAnnotation(PrimaryKey.class);
                                if (!pk.name().equals("")) {
                                    referenceField = pk.name();
                                }
                                fkField = f;
                                break;
                            }
                        }

                        referenceField = referenceField.toLowerCase();
                        reference = reference.toLowerCase() + "_" + referenceField;
                        if (!TextUtils.isEmpty(fk.name())) {
                            reference = fk.name();
                        }

                        Object fkObject = field.getType().newInstance();
                        PropertyUtils fkPropertyUtils = new PropertyUtils(fkObject);
                        this._setValue(fkPropertyUtils, fkField, referenceField, reference);
                        propertyUtils.setProperty(field.getName(), fkObject);
                        continue;
                    } else if (isColumn) {
                        Column c = field.getAnnotation(Column.class);
                        if (!TextUtils.isEmpty(c.name())) {
                            columnName = c.name();
                        }
                    }
                    this._setValue(propertyUtils, field, name, columnName);
                }
            }
            return entity;
        } catch (Exception e) {
            Log.e(AbstractRepository.class.getSimpleName(), "Erro ao tentar obter informação do getCursor() : " + logName);
            e.printStackTrace();
            return null;
        }
    }

    private void _setValue(PropertyUtils pu, Field field, String name, String column) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Type type = field.getGenericType();
        if (type instanceof Class && ((Class<?>) type).isEnum()) {
            pu.setProperty(name, field.getType().getEnumConstants()[getInt(column)]);
        } else if (type == Integer.TYPE) {
            pu.setProperty(name, getInt(column));
        } else if (type == Double.TYPE) {
            pu.setProperty(name, getDouble(column));
        } else if (type == Float.TYPE) {
            pu.setProperty(name, getFloat(column));
        } else if (type == Long.TYPE || type == Long.class) {
            pu.setProperty(name, getLong(column));
        } else if (type == Boolean.TYPE) {
            pu.setProperty(name, getBoolean(column));
        } else if (type == Date.class) {
            if (getLong(name) != null) {
                pu.setProperty(name, new Date(getLong(column)));
            }
        } else {
            pu.setProperty(name, getString(column));
        }
    }

    private String[] columns() {
        Set<String> fields = new HashSet<>();
        for (Field field : klass.getDeclaredFields()) {
            boolean isPrimaryKey = field.isAnnotationPresent(PrimaryKey.class);
            boolean isForeingKey = field.isAnnotationPresent(ForeignKey.class);
            boolean isColumn = field.isAnnotationPresent(Column.class);
            boolean isTransient = field.isAnnotationPresent(Transient.class);
            if (!isTransient && !field.isSynthetic()) {
                String name = field.getName().toLowerCase();

                if (!propertyUtils.exists(name)) {
                    continue;
                }

                if (isPrimaryKey) {
                    PrimaryKey pk = field.getAnnotation(PrimaryKey.class);
                    if (!TextUtils.isEmpty(pk.name())) {
                        name = pk.name();
                    }
                } else if (isForeingKey) {
                    ForeignKey fk = field.getAnnotation(ForeignKey.class);
                    String reference = field.getType().getSimpleName();
                    String referenceField = "";
                    for (Field f : field.getType().getDeclaredFields()) {
                        if (f.isAnnotationPresent(PrimaryKey.class)) {
                            referenceField = f.getName();
                            PrimaryKey pk = f.getAnnotation(PrimaryKey.class);
                            if (!pk.name().equals("")) {
                                referenceField = pk.name();
                            }
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
        this.propertyUtils = new PropertyUtils(entity);

        ContentValues values = new ContentValues();
        for (Field field : klass.getDeclaredFields()) {
            try {
                boolean isTransient = field.isAnnotationPresent(Transient.class);
                String fieldName = field.getName().toLowerCase();
                if (!isTransient && !field.isSynthetic()) {
                    if (!propertyUtils.exists(fieldName)) {
                        continue;
                    }

                    Object value = propertyUtils.getProperty(fieldName);
                    if (value == null || String.valueOf(value).equalsIgnoreCase("NULL"))
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
                        for (Field f : field.getType().getDeclaredFields()) {
                            if (f.isAnnotationPresent(PrimaryKey.class)) {
                                referenceField = f.getName();
                                PrimaryKey pk = f.getAnnotation(PrimaryKey.class);
                                if (!pk.name().equals("")) {
                                    referenceField = pk.name();
                                }
                            }
                        }
                        reference = reference.toLowerCase() + "_" + referenceField.toLowerCase();
                        if (!TextUtils.isEmpty(fk.name())) {
                            reference = fk.name();
                        }
                        name = reference;
                        /**
                         *  Precisa desse campo por causa da PropertyUtils FINAL que responde a
                         *  classe da instancia, por esse código ser somenten da FK é necessário
                         *  gerar um novo mapeamento.
                         *
                         *  Verificar uma forma de deixar isso mais rápido no futuro.
                         */
                        PropertyUtils fkPropertyUtils = new PropertyUtils(value);
                        value = fkPropertyUtils.getProperty(referenceField);
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
    public long insert(T entity) {
        long id = -1;
        try {
            id = getSqLiteDatabase().insert(table(), null, getValues(entity));
            commit();
        } finally {
            closeSQLiteDatabase();
            return id;
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

    @Override
    public void update(T entity) throws NoPrimaryKeyFoundException, NoPrimaryKeyValueFoundException {
        try {
            getSqLiteDatabase().update(table(), getValues(entity), this.getPrimaryKey() + " = ?", new String[]{String.valueOf( getPrimaryValue() )});
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
    public void delete(T entity) throws NoPrimaryKeyFoundException, NoPrimaryKeyValueFoundException {
        try {
            this.propertyUtils = new PropertyUtils(entity);
            getSqLiteDatabase().delete(table(), this.getPrimaryKey() + " = ?", new String[]{String.valueOf( getPrimaryValue() )});
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
        return list(null, null, null, -1, -1);
    }

    @Override
    public List<T> list(int limit) {
        return list(null, null, null, limit, -1);
    }

    @Override
    public List<T> list(String orderBy) {
        return list(null, null, orderBy, -1, -1);
    }

    @Override
    public List<T> list(String fields, String[] values) {
        return list(fields, values, null, -1, -1);
    }

    @Override
    public List<T> list(String fields, String[] values, String orderBy) {
        return list(fields, values, orderBy, -1, -1);
    }

    @Override
    public List<T> list(String fields, String[] values, String orderBy, int limit) {
        return list(fields, values, orderBy, limit, -1);
    }

    @Override
    public List<T> list(String fields, String[] values, String orderBy, int limit, int offset) {
        try {
            List<T> entity = new ArrayList<>();

            String offsetLimit = null;
            if (offset != -1) {
                offsetLimit = offset + "," + limit;
            } else if (limit != -1) {
                offsetLimit = String.valueOf(limit);
            }

            cursor = getSqLiteDatabase(MODE.READABLE).query(table(), columns(), fields, values, null, null, orderBy, offsetLimit);
            while (cursor.moveToNext()) {
                entity.add(getCursor());
            }
            cursor.close();
            commit();
            return entity;
        } finally {
            if (cursor != null && !cursor.isClosed())
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
            if (cursor != null && !cursor.isClosed())
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

            cursor = getSqLiteDatabase(MODE.READABLE).rawQuery(sql, values);
            while (cursor.moveToNext()) {
                contagem = cursor.getInt(0);
            }
            cursor.close();
            commit();
            return contagem;
        } finally {
            if (cursor != null && !cursor.isClosed())
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
