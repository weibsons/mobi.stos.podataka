package mobi.stos.podataka.test.bo;

import android.content.Context;

import mobi.stos.podataka.test.bean.Montadora;
import mobi.stos.podataka.test.dao.MontadoraDao;
import mobi.stos.podataka_lib.interfaces.IOperations;
import mobi.stos.podataka_lib.service.AbstractService;

/**
 * Created by links_000 on 19/04/2016.
 */
public class MontadoraBo extends AbstractService<Montadora> {

    private MontadoraDao dao;

    public MontadoraBo(Context context) {
        super();
        this.dao = new MontadoraDao(context);
    }

    @Override
    protected IOperations<Montadora> getDao() {
        return dao;
    }
}
