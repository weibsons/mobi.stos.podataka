Biblioteca de mapeamento de entidades para geração e consultas SQL para dispositivos Android
===============================================

Descrição
---------
Biblioteca que mapeia classes Java que possuem a anotação @Entity para criar as tabelas em sqlite e realizar procedimento de SELECT, INSERT, UPDATE, DELETE.


Configurando seu Projeto
------------------------

Gradle:
```gradle

android {
  ...
  
  packagingOptions {
        exclude 'META-INF/LICENSE.txt'
        exclude 'META-INF/NOTICE.txt'
  }
  
}

...

dependencies {
    ...
    compile 'br.com.uol.ps:library:0.4'
    compile 'com.google.code.gson:gson:+'
    ...
}

```

h2. *Configurando o Projeto*

Adicione a dependência no build.gradle do seu projeto
Inclua entre os informativos do *android { }* a seguinte instrução:

packagingOptions {
      exclude 'META-INF/LICENSE.txt'
      exclude 'META-INF/NOTICE.txt'
}

Estruturas de mapeamento:
Classe de teste do PODATAKA.

@Entity
	As entidades devem possuir o@Entity como representação do seu método.
	Cada tabela do banco de dados possuirá o nome de sua entidade.

@Column
	Dentro de cada entidade o objeto mais simples é o@Column esse objeto pode ser surpimidido.
	Caso o @Column não exista por padrão será gerado um CHARACTER VARING de 255 posições

@PrimaryKey
	Anotação que existe dentro da entidade que representa a chave primária. Por padrão ela é auto incremental mas pode ser ajustado.

@ForeignKey
	Anotação que representa a chave estrangeira

@Transient
	Anotação que representa objetos que não será persistidos no banco de dados, somente existirá para utilização na entidade.

Seguindo o padrão Repositório e Serviço:

	As classes que forem responsáveis de acesso ao banco de dados deve ser extendidas de AbstractRepository
	As classes que forem responsáveis a regra de negócio / serviços deve ser extendiddas de AbstractService
