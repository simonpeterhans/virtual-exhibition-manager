package ch.unibas.dmi.dbis.vrem.model.objects;

import java.util.HashMap;
import java.util.Map;
import org.bson.types.ObjectId;

public class CulturalHeritageObject {

    public final String id;

    public String name;

    public final CHOType type;

    public String path;

    public String description;

    public final Map<String, String> metadata = new HashMap<>();

    /**
     *
     */
    public CulturalHeritageObject(ObjectId id, String name, String description, String path, CHOType type) {
        this.id = id.toHexString();
        this.name = name;
        this.description = description;
        this.path = path;
        this.type = type;
    }

    /**
     *
     */
    public CulturalHeritageObject(String name, String description, String path, CHOType type) {
        this(new ObjectId(), name, description, path, type);
    }

    /**
     *
     */
    public enum CHOType {
        IMAGE, MODEL
    }
}
