Biblioteca de mapeamento de entidades para geração e consultas SQL para dispositivos Android
===============================================

Descrição
---------
Biblioteca que mapeia classes Java que possuem a anotação @Entity para criar as tabelas em sqlite e realizar procedimento de SELECT, INSERT, UPDATE, DELETE.


Configurando seu Projeto
------------------------

Gradle:
```gradle

dependencies {
    compile 'mobi.stos:podataka_lib:0.1'
    compile 'org.reflections:reflections:0.9.10'
}

```

Anotações de Mapeamento
-----------------------

##### @Entity 
As entidades devem possuir o @Entity como representação do seu método.
Cada tabela do banco de dados possuirá o nome de sua entidade.

##### @Column
Dentro de cada entidade o objeto mais simples é o @Column esse objeto não é obrigatório.

##### @PrimaryKey
Anotação que existe dentro da entidade que representa a chave primária. Por padrão ela é auto incremental mas pode ser ajustado. Essa anotação representará a condição WHERE para o UPDATE e DELETE.

##### @ForeignKey
Anotação que representa a chave estrangeira.

##### @Transient
Anotação que representa objetos que não será persistidos no banco de dados, somente existirá para utilização na entidade.


Exemplo de como trabalhar com PODATAKA em seu projeto
----------------------------------------------------------

##### Bean
Classes básicas de sua aplicação, essas classes serão espelho de sua tabela do banco de dados e deve possuir as anotações de mapeamento para correta construção do banco de dados com INSERT, DELETE, UPDATE e SELECT.

As classes beans devem implementar `Serializable`

Exemplo:

```java

@Entity
public class Montadora implements Serializable {

    @PrimaryKey
    private int id;
    @Column(nullable = false, length = 50)
    private String nome;
    private boolean status;
    
    ...
     getters and setters
    ...
}

@Entity
public class Carro implements Serializable {

    @PrimaryKey
    private int id;
    @ForeignKey(nullable = false)
    private Montadora montadora;
    @Column(nullable = false, length = 7)
    private String placa;
    @Column(nullable = false, length = 20)
    private String cor;
    private int anoFabricacao;
    private int anoModelo;
    
    ...
     getters and setters
    ...
}

```


##### Operations
Classes de abstração das operações `IOperations` devem ser associadas aos **Repositórios** e **Serviços**

Essas operações contem funcionalidades básicas do sistema que serão utilizadas pelas classes `AbstractRepository` e `AbstractService`

Funções de `IOperations<T>`

```java
void insert(T entity);

void insert(List<T> list);

void update(T entity) throws NoPrimaryKeyFoundException, NoPrimaryKeyValueFoundException;

void delete(T entity) throws NoPrimaryKeyFoundException, NoPrimaryKeyValueFoundException;

T get(String fields, String[] values);

List<T> list();

List<T> list(int limit);

List<T> list(String orderBy);

List<T> list(String fields, String[] values);

List<T> list(String fields, String[] values, String orderBy);

List<T> list(String fields, String[] values, String orderBy, int limit);

int count(String fields, String[] values);

void clean();
```

##### Repository
As classes de Repositório utilizar extendidas de `AbstractRepository`

Exemplo:

```java

public interface ICarroDao extends IOperations<Carro> {
}

public class CarroDao extends AbstractRepository<Carro> implements ICarroDao {

    public CarroDao(Context context) {
        super(context, Carro.class);
    }
}

```

##### Service
As classes de Serviço utilizar extendidas de `AbstractService`

```java

public interface ICarroBo extends IOperations<Carro> {
}

public class CarroBo extends AbstractService<Carro> implements ICarroBo {

    private ICarroDao dao;

    public CarroBo(Context context) {
        super();
        this.dao = new CarroDao(context);
    }

    @Override
    protected IOperations<Carro> getDao() {
        return dao;
    }
    
    ...
}


```

Configurando o Mapeamento
-------------------------

A parte essencial do projeto é o mapeamento de suas entidades. Infelizmente o Android não está permitindo o scan dos pacotes (quem conseguir fazer seria de boa ajuda) e por hora será necessário mapear as classes Entidades manualmente (como no antigo mapeamento do Hibernate).

Dentro do diretório `src/main/res/values/` crie um arquivo chamado `db.xml` com a seguinte estrutura:

```xml
<resources>
    <string name="db_name">sample-db</string>
    <integer name="db_version">1</integer>
    <string-array name="db_scan">
        <item>mobi.stos.podataka.test.bean.Carro</item>
        <item>mobi.stos.podataka.test.bean.Montadora</item>
    </string-array>
    <string name="db_log">false</string>
</resources>
```

Onde:

**db_name**

Será o nome do seu banco de dados

**db_version**

A versão do seu banco de dados, como é realizado no padrão, usar números inteiro.

**db_log**

Se você quer exibir o Log das consultas SQL geradas.

**db_scan**

Cada item representa o endereço do seu package + nome da Classe conforme apresentado no exemplo.
Lembrando que só serão mapeada as classes que possuirem a annotation @Entity





Licença
-------

Copyright 2016 S'tos Sociedade LTDA.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.


Contribuições
-------------

Achou e corrigiu um bug ou tem alguma feature em mente e deseja contribuir?

* Faça um fork.
* Adicione sua feature ou correção de bug.
* Envie um pull request no [GitHub].

**S'tos App**

* Nossa Página: http://stos.mobi/

* Nossos Apps: https://play.google.com/store/apps/dev?id=9117205727352262184
