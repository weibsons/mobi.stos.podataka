package mobi.stos.podataka_lib.interfaces;

import java.io.Serializable;
import java.util.List;

import mobi.stos.podataka_lib.exception.NoPrimaryKeyFoundException;
import mobi.stos.podataka_lib.exception.NoPrimaryKeyValueFoundException;

public interface IOperations<T extends Serializable> {

    /***
     * Função representa persistência de somente 1 entidade por vez
     * @param entity Entity
     * @return long ID do registro
     */
    long insert(T entity);

    /***
     * Função representa a persistência de uma lista de objetos numa mesma conexão.
     * Em caso de falha ou má peenchido de um objeto todos será abortados por estarem dentro da mesma
     * transção não comitada.
     * @param list List
     */
    void insert(List<T> list);

    /***
     *
     * Use this code only if you have a simple primary key.
     * For complex primary key create your own method
     *
     * Função responsável por atualizar os registros de tabela com base a chave primária.
     * @param entity T
     * @throws NoPrimaryKeyFoundException Caso não seja encontrado a anotação de PrimaryKey em sua entidade
     * @throws NoPrimaryKeyValueFoundException Caso o valor da PrimaryKey não esteja sido preenchido no objeto que deseja atualizar
     */
    void update(T entity) throws NoPrimaryKeyFoundException, NoPrimaryKeyValueFoundException;

    /***
     *
     * Use this code only if you have a simple primary key.
     * For complex primary key create your own method
     *
     * @param entity T
     * @throws NoPrimaryKeyFoundException Caso não seja encontrado a anotação de PrimaryKey em sua entidade
     * @throws NoPrimaryKeyValueFoundException Caso o valor da PrimaryKey não esteja sido preenchido no objeto que deseja excluir
     */
    void delete(T entity) throws NoPrimaryKeyFoundException, NoPrimaryKeyValueFoundException;

    T get(String fields, String[] values);
    
    List<T> list();
    
    List<T> list(int limit);

    List<T> list(String orderBy);

    List<T> list(String fields, String[] values);

    List<T> list(String fields, String[] values, String orderBy);

    List<T> list(String fields, String[] values, String orderBy, int limit);

    List<T> list(String fields, String[] values, String orderBy, int limit, int offset);

    /***
     * Função conta registro da tabela com base a critéria informada. Caso não seja passado null será
     * feita uma contagem total de registro da tabela.
     * @param fields String
     * @param values String[]
     * @return int Número de registros registrados no banco de dados conforme clausula
     */
    int count(String fields, String[] values);

    /***
     * Função limpa os objetos de uma tabela.
     * exec: DELETE FROM [table];
     * Não define cláusula WHERE
     */
    void clean();
}
