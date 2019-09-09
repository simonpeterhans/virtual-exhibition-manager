package ch.unibas.dmi.dbis.vrem.model;

import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibit;
import java.util.List;

public class ListExhibitsResponse {

    public final List<Exhibit> exhibits;

    public ListExhibitsResponse(List<Exhibit> exhibits) {
        this.exhibits = exhibits;
    }

    public List<Exhibit> getExhibits() {
        return exhibits;
    }
}
