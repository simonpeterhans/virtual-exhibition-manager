package ch.unibas.dmi.dbis.vrem.model.collection;


import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;

public class ArtCollection {

    public final String id;
    public String name;
    public final Map<String, String> metadata = new HashMap<>();
    public List<Exhibit> exhibits;

    public ArtCollection(ObjectId id, String name, List<Exhibit> exhibits) {
        this.id = id.toHexString();
        this.name = name;
        this.exhibits = exhibits;
    }

    public ArtCollection(String name, List<Exhibit> exhibits) {
        this(new ObjectId(), name, exhibits);
    }

    public List<Exhibit> getExhibits() {
        if (exhibits == null) {
            return Collections.unmodifiableList(new ArrayList<>());
        }
        return Collections.unmodifiableList(this.exhibits);
    }

}
