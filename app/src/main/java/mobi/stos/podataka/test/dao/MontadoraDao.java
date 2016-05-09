package mobi.stos.podataka.test.dao;


import android.content.Context;

import mobi.stos.podataka.test.bean.Montadora;
import mobi.stos.podataka_lib.repository.AbstractRepository;

/**
 * Created by links_000 on 19/04/2016.
 */
public class MontadoraDao extends AbstractRepository<Montadora> {

    public MontadoraDao(Context context) {
        super(context, Montadora.class);
    }
}
