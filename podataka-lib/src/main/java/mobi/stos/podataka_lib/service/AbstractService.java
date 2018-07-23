package mobi.stos.podataka_lib.service;

import java.io.Serializable;
import java.util.List;

import mobi.stos.podataka_lib.exception.NoPrimaryKeyFoundException;
import mobi.stos.podataka_lib.exception.NoPrimaryKeyValueFoundException;
import mobi.stos.podataka_lib.interfaces.IOperations;


public abstract class AbstractService<T extends Serializable> implements IOperations<T> {

    protected abstract IOperations<T> getDao();

    @Override
    public long insert(T entity) {
        return getDao().insert(entity);
    }

    @Override
    public void insert(List<T> entities) {
        getDao().insert(entities);
    }

    @Override
    public void update(T entity) throws NoPrimaryKeyFoundException, NoPrimaryKeyValueFoundException {
        getDao().update(entity);
    }

    @Override
    public void delete(T entity) throws NoPrimaryKeyFoundException, NoPrimaryKeyValueFoundException {
        getDao().delete(entity);
    }

    @Override
    public T get(String fields, String[] values) {
        return getDao().get(fields, values);
    }

    @Override
    public List<T> list() {
        return getDao().list();
    }

    @Override
    public List<T> list(int limit) {
        return getDao().list(limit);
    }

    @Override
    public List<T> list(String orderBy) {
        return getDao().list(orderBy);
    }

    @Override
    public List<T> list(String fields, String[] values) {
        return getDao().list(fields, values);
    }

    @Override
    public List<T> list(String fields, String[] values, String orderBy) {
        return getDao().list(fields, values, orderBy);
    }

    @Override
    public List<T> list(String fields, String[] values, String orderBy, int limit) {
        return getDao().list(fields, values, orderBy, limit);
    }

    @Override
    public List<T> list(String fields, String[] values, String orderBy, int limit, int offset) {
        return getDao().list(fields, values, orderBy, limit, offset);
    }

    @Override
    public int count(String fields, String[] values) {
        return getDao().count(fields, values);
    }

    @Override
    public void clean() {
        getDao().clean();
    }
}
