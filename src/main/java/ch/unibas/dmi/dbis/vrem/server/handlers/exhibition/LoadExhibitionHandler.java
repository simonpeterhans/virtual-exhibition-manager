package ch.unibas.dmi.dbis.vrem.server.handlers.exhibition;

import ch.unibas.dmi.dbis.vrem.database.dao.VREMReader;
import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibition;
import ch.unibas.dmi.dbis.vrem.server.handlers.basic.ParsingActionHandler;
import java.util.Arrays;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;

public class LoadExhibitionHandler extends ParsingActionHandler<Exhibition> {

    private final VREMReader reader;

    private final static String ATTRIBUTE_ID = ":id";

    private final static String ATTRIBUTE_KEY = ":key";

    private final static Logger LOGGER = LogManager.getLogger(LoadExhibitionHandler.class);

    public LoadExhibitionHandler(VREMReader reader) {
        this.reader = reader;
    }

    @Override
    public Exhibition doGet(Map<String, String> parameters) {
        Exhibition exhibition = null;
        if (parameters.containsKey(ATTRIBUTE_ID)) {
            final ObjectId objectId = new ObjectId(parameters.get(ATTRIBUTE_ID));
            LOGGER.debug("Loading exhibition by objectID {}", objectId);
            exhibition = this.reader.getExhibition(objectId);
        }
        if (parameters.containsKey(ATTRIBUTE_KEY) && exhibition == null) {
            final String key = parameters.get(ATTRIBUTE_KEY);
            LOGGER.debug("Loading exhibition by key {}", key);
            exhibition = this.reader.getExhibition(key);
        }
        if (exhibition == null) {
            LOGGER.warn("No exhibition found for parameter values {}", Arrays.toString(parameters.values().toArray()));
        } else {
            LOGGER.debug("Loaded exhibition with name {} successfully", exhibition.name);
        }
        return exhibition;
    }

    @Override
    public Class<Exhibition> inClass() {
        return Exhibition.class;
    }
}

