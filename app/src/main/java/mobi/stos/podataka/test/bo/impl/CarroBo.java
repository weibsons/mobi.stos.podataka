package mobi.stos.podataka.test.bo.impl;

import android.content.Context;

import mobi.stos.podataka.test.bean.Carro;
import mobi.stos.podataka.test.bo.ICarroBo;
import mobi.stos.podataka.test.dao.ICarroDao;
import mobi.stos.podataka.test.dao.impl.CarroDao;
import mobi.stos.podataka_lib.interfaces.IOperations;
import mobi.stos.podataka_lib.service.AbstractService;

/**
 * Created by links_000 on 19/04/2016.
 */
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
}
