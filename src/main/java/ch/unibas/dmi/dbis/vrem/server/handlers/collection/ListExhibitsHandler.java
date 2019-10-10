package ch.unibas.dmi.dbis.vrem.server.handlers.collection;

import ch.unibas.dmi.dbis.vrem.database.dao.VREMReader;
import ch.unibas.dmi.dbis.vrem.model.ListExhibitsResponse;
import ch.unibas.dmi.dbis.vrem.server.handlers.basic.ActionHandlerException;
import ch.unibas.dmi.dbis.vrem.server.handlers.basic.ParsingActionHandler;
import java.util.List;
import java.util.Map;

public class ListExhibitsHandler extends ParsingActionHandler<List> {


    private final VREMReader reader;

    /**
     *
     */
    public ListExhibitsHandler(VREMReader reader) {
        this.reader = reader;
    }


    @Override
    public Object doGet(Map<String, String> parameters) throws ActionHandlerException {
        return new ListExhibitsResponse(this.reader.listExhibits());
    }

    @Override
    public Class<List> inClass() {
        return List.class;
    }
}
