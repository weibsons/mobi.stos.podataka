package mobi.stos.podataka_lib.interfaces;

import java.io.Serializable;
import java.util.List;

import mobi.stos.podataka_lib.exception.NoPrimaryKeyFoundException;
import mobi.stos.podataka_lib.exception.NoPrimaryKeyValueFoundException;

public interface IOperations<T extends Serializable> {

    /***
     * Função representa persistência de somente 1 entidade por vez
     * @param entity Entity
     */
    void insert(T entity);

    /***
     * Função representa a persistência de uma lista de objetos numa mesma conexão.
     * Em caso de falha ou má peenchido de um objeto todos será abortados por estarem dentro da mesma
     * transção não comitada.
     * @param list
     */
    void insert(List<T> list);

    /***
     *
     * Use this code only if you have a simple primary key.
     * For complex primary key create your own method
     *
     * Função responsável por atualizar os registros de tabela com base a chave primária.
     * @param entity
     * @throws NoPrimaryKeyFoundException
     * @throws NoPrimaryKeyValueFoundException
     */
    void update(T entity) throws NoPrimaryKeyFoundException, NoPrimaryKeyValueFoundException;

    /***
     *
     * Use this code only if you have a simple primary key.
     * For complex primary key create your own method
     *
     * @param entity
     * @throws NoPrimaryKeyFoundException
     * @throws NoPrimaryKeyValueFoundException
     */
    void delete(T entity) throws NoPrimaryKeyFoundException, NoPrimaryKeyValueFoundException;

    T get(String fields, String[] values);
    
    List<T> list();
    
    List<T> list(int limit);

    List<T> list(String orderBy);

    List<T> list(String fields, String[] values);

    List<T> list(String fields, String[] values, String orderBy);

    List<T> list(String fields, String[] values, String orderBy, int limit);

    /***
     * Função conta registro da tabela com base a critéria informada. Caso não seja passado null será
     * feita uma contagem total de registro da tabela.
     * @param fields
     * @param values
     * @return
     */
    int count(String fields, String[] values);

    /***
     * Função limpa os objetos de uma tabela.
     * exec: DELETE FROM [table];
     * Não define cláusula WHERE
     */
    void clean();
}
