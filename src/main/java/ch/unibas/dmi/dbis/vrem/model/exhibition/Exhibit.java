package ch.unibas.dmi.dbis.vrem.model.exhibition;

import ch.unibas.dmi.dbis.vrem.model.Vector3f;
import ch.unibas.dmi.dbis.vrem.model.objects.CulturalHeritageObject;
import org.bson.types.ObjectId;

public class Exhibit extends CulturalHeritageObject {

    public Vector3f position = Vector3f.NaN;
    public Vector3f size = Vector3f.NaN;
    public final String audio;
    public final boolean light;

    public Exhibit(ObjectId id, String name, String description, String path, CHOType type) {
        this(id, name, description, path, type, Vector3f.ORIGIN, Vector3f.UNIT, null, false);
    }

    public Exhibit(ObjectId id, String name, String description, String path, CHOType type, Vector3f position, Vector3f size) {
        this(id, name, description, path, type, position, size, null, false);
    }

    public Exhibit(ObjectId id, String name, String description, String path, CHOType type, Vector3f position, Vector3f size, String audio, boolean light) {
        super(id, name, description, path, type);
        this.size = size;
        this.position = position;
        this.audio = audio;
        this.light = light;
    }

    public Exhibit(String name, String description, String path, CHOType type) {
        this(new ObjectId(), name, description, path, type);
    }

    public Exhibit(String name, String description, String path, CHOType type, Vector3f position, Vector3f size) {
        this(new ObjectId(), name, description, path, type, position, size);
    }
    
    public Exhibit(String name, String description, String path, CHOType type, Vector3f position, Vector3f size, String audio, boolean light) {
        this(new ObjectId(), name, description, path, type, position, size, audio, light);
    }
}
