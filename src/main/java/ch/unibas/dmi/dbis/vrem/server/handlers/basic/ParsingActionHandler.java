package ch.unibas.dmi.dbis.vrem.server.handlers.basic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import org.bson.types.ObjectId;
import spark.Request;
import spark.Response;

public abstract class ParsingActionHandler<A> implements ActionHandler<A> {

    /**
     * Invoked when an incoming request is routed towards this class by Java Spark. The method handles that request, extracts named parameters and parses the (optional) request body using Jackson. The resulting context object is then forwarded to the doGet() method.
     *
     * @param request The request object providing information about the HTTP request
     * @param response The response object providing functionality for modifying the response
     * @return The content to be set in the response
     * @throws Exception implementation can choose to throw exception
     */
    @Override
    public Object handle(Request request, Response response) throws Exception {
        try {
            Map<String, String> params = request.params();

            if (params == null) {
                params = new HashMap<>();
            }
            response.type("application/json");

            GsonBuilder builder = new GsonBuilder();
            JsonSerializer<ObjectId> serializer = new JsonSerializer<ObjectId>() {
                @Override
                public JsonElement serialize(ObjectId src, Type typeOfSrc, JsonSerializationContext context) {
                    JsonObject json = new JsonObject();
                    json.addProperty("id", src.toHexString());
                    return json;
                }
            };
            JsonDeserializer<ObjectId> deserializer = new JsonDeserializer<ObjectId>() {
                @Override
                public ObjectId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    if (json instanceof JsonObject) {
                        JsonObject obj = (JsonObject) json;
                        return new ObjectId(obj.get("id").getAsString());
                    }
                    throw new JsonParseException("Couldn't parse objectId: " + json);
                }
            };

            builder.registerTypeAdapter(ObjectId.class, serializer);
            builder.registerTypeAdapter(ObjectId.class, deserializer);

            final Gson gson = builder.create();

            switch (request.requestMethod()) {
                case "GET":
                    return gson.toJson(this.doGet(params));
                case "DELETE":
                    this.doDelete(params);
                    return null;
                case "POST":
                    return gson.toJson(this.doPost(gson.fromJson(request.body(), this.inClass()), params));
                case "PUT":
                    return gson.toJson(this.doPut(gson.fromJson(request.body(), this.inClass()), params));
                default:
                    throw new MethodNotSupportedException(request);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

