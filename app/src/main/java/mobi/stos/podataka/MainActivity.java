package mobi.stos.podataka;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import mobi.stos.podataka.test.bean.Carro;
import mobi.stos.podataka.test.bean.Montadora;
import mobi.stos.podataka.test.bo.CarroBo;
import mobi.stos.podataka.test.bo.MontadoraBo;
import mobi.stos.podataka_lib.exception.NoPrimaryKeyFoundException;
import mobi.stos.podataka_lib.exception.NoPrimaryKeyValueFoundException;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final boolean EXEC_TEST = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /***
         *
         * ------------------- CONFIGURANDO O PROJETO -----------------
         *
         * Adicione a dependencia no build.gradle do seu projeto
         *
         * Inclua entre os informativos do android { } a seguinte instrução:
         *
         *  packagingOptions {
         *      exclude 'META-INF/LICENSE.txt'
         *      exclude 'META-INF/NOTICE.txt'
         *  }
         *
         * Isso evitará conflitos
         *
         *
         *
         * Classe de teste do PODATAKA.
         *
         * @Entity
         *  As entidades devem possuir o @Entity como representação do seu método.
         *  Cada tabela do banco de dados possuirá o nome de sua entidade.
         *
         * @Column
         *  Dentro de cada entidade o objeto mais simples é o @Column esse objeto pode ser surpimidido.
         *  Caso o @Column não exista por padrão será gerado um CHARACTER VARING de 255 posições
         *
         * @PrimaryKey
         *  Anotação que existe dentro da entidade que representa a chave primária. Por padrão ela é auto incremental
         *  mas pode ser ajustado.
         *
         * @ForeignKey
         *  Anotação que representa a chave estrangeira
         *
         * @Transient
         *  Anotação que representa objetos que não será persistidos no banco de dados, somente existirá
         *  para utilização na entidade.
         *
         *  Seguindo o padrão Repositório e Serviço:
         *   As classes que forem responsáveis de acesso ao banco de dados deve ser extendidas de AbstractRepository
         *   As classes que forem responsáveis a regra de negócio / serviços deve ser extendiddas de AbstractService
         *
         * 1. Instancia-se os serviços
         * 2. Preenche as entidades
         *
         *
         */

        if (EXEC_TEST) {
            test2();
        }
    }

    private void test3() {
        Log.v(TAG, "************** INICIALIZANDO **************** ");

        MontadoraBo montadoraBo = new MontadoraBo(this);
        Montadora sample = montadoraBo.get("nome = ?", new String[]{"Fiat"});

        CarroBo carroBo = new CarroBo(this);
        carroBo.insert(new Carro(sample, "TST0002", "Vermelho", 2014, 2014));

        List<Carro> carros = carroBo.list();
        for (Carro carro : carros) {
            Log.v(TAG, "Carro -> " + carro.getId() + " .. " + carro.getPlaca() + " .. " + carro.getCor());
        }


        Log.v(TAG, "************** FIM DO PROCESSO **************** ");

        Toast.makeText(this, "Veja o log para mais detalhes", Toast.LENGTH_LONG).show();
    }

    private void test2() {
        Log.v(TAG, "************** INICIALIZANDO **************** ");

        MontadoraBo montadoraBo = new MontadoraBo(this);
        Montadora sample = montadoraBo.get("nome = ?", new String[]{"Fiat"});

        CarroBo carroBo = new CarroBo(this);
        carroBo.insert(new Carro(sample, "TST0001", "Vermelho", 2014, 2014, "TESTE-CHASSI-110"));

        List<Carro> carros = carroBo.list();
        for (Carro carro : carros) {
            Log.v(TAG, "Carro -> " + carro.getId() + " .. " + carro.getPlaca() + " .. " + carro.getCor());
        }


        Log.v(TAG, "************** FIM DO PROCESSO **************** ");

        Toast.makeText(this, "Veja o log para mais detalhes", Toast.LENGTH_LONG).show();
    }

    private void test() {
        Log.v(TAG, "************** INICIALIZANDO **************** ");

        MontadoraBo montadoraBo = new MontadoraBo(this);
        montadoraBo.clean();
        CarroBo carroBo = new CarroBo(this);

        long id = montadoraBo.insert(new Montadora("Fiat", true));
        Log.v(TAG, "MONTADORA: " + id);
        montadoraBo.get(null, null);

        Montadora sample = montadoraBo.get("nome = ?", new String[]{"Fiat"});
        Log.v(TAG, "status: " + sample.isStatus());

        Log.v(TAG, "Carregando os carros ");
        List<Carro> carros = new ArrayList<>();
        carros.add(new Carro(sample, "KKE1062", "Preto", 2008, 2009));
        carros.add(new Carro(sample, "KLY837", "Branco", 2014, 2014));
        carroBo.insert(carros);

        Carro rep = null;
        Log.v(TAG, "Listando...");
        for (Carro carro : carroBo.list(10)) {
            Log.v(TAG, carro.getPlaca() + " - " + carro.getAnoFabricacao() + " - " + carro.getMontadora().getId());

            rep = carro;
        }
        int ano = (int) (Math.random() * 9999);
        Log.v(TAG, "Novo ano que será setado o ultimo registro: " + ano);
        rep.setAnoFabricacao(ano);
        try {
            carroBo.update(rep);
        } catch (NoPrimaryKeyFoundException e) {
            e.printStackTrace();
        } catch (NoPrimaryKeyValueFoundException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "After update");
        for (Carro carro : carroBo.list()) {
            Log.v(TAG, carro.getPlaca() + " - " + carro.getAnoFabricacao() + " - " + carro.getMontadora().getId());
        }

        Log.v(TAG, "************** FIM DO PROCESSO **************** ");

        Toast.makeText(this, "Veja o log para mais detalhes", Toast.LENGTH_LONG).show();
    }

}
