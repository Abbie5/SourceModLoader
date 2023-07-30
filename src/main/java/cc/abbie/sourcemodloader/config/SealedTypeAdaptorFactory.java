package cc.abbie.sourcemodloader.config;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

// credit goes to luna
// thanks luna!

public class SealedTypeAdaptorFactory<T> implements TypeAdapterFactory {

	private final String typeFieldName;
	private final HashMap<String, Class<?>> nameToSubclass = new HashMap<>();
	private final Class<?>[] subclasses;
	private final String baseType;

	public SealedTypeAdaptorFactory(Class<T> baseClass, String typeFieldName) {
		if (!baseClass.isSealed() || Arrays.stream(baseClass.getPermittedSubclasses()).findAny().isEmpty())
			throw new IllegalArgumentException("Must be of a sealed type with sealed members");
		Arrays.stream(baseClass.getPermittedSubclasses()).forEach(
			(subclass) -> nameToSubclass.put(subclass.getSimpleName(), subclass)
		);
		this.typeFieldName = typeFieldName;
		this.subclasses = baseClass.getPermittedSubclasses();
		baseType = baseClass.getSimpleName();
	}

	@Override
	public <R> TypeAdapter<R> create(Gson gson, TypeToken<R> type) {
		if (type == null || Arrays.stream(subclasses).noneMatch(type.getRawType()::isAssignableFrom)) return null;

		TypeAdapter<JsonElement> elementTypeAdapter = gson.getAdapter(JsonElement.class);
		HashMap<Class<?>, TypeAdapter<?>> subclassToAdapter = new HashMap<>();
		Arrays.stream(subclasses).forEach(
			subclass -> subclassToAdapter.put(subclass, gson.getDelegateAdapter(this, TypeToken.get(subclass)))
		);

		return new TypeAdapter<>() {

			@SuppressWarnings("unchecked")
			@Override
			public void write(JsonWriter out, R value) throws IOException {
				Class<R> srcType = (Class<R>) value.getClass();
				String label = srcType.getSimpleName();
				TypeAdapter<R> delegate = (TypeAdapter<R>) subclassToAdapter.get(srcType);
				JsonObject jsonObject = delegate.toJsonTree(value).getAsJsonObject();

				if (jsonObject.has(typeFieldName)) {
					throw new JsonParseException("cannot serialize " + label + " because it already defines a field named " + typeFieldName);
				}
				JsonObject clone = new JsonObject();
				clone.add(typeFieldName, new JsonPrimitive(label));
				for (HashMap.Entry<String, JsonElement> it : jsonObject.entrySet()) {
					clone.add(it.getKey(), it.getValue());
				}
				elementTypeAdapter.write(out, clone);
			}

			@SuppressWarnings("unchecked")
			@Override
			public R read(JsonReader in) throws IOException {
				JsonElement element = elementTypeAdapter.read(in);
				JsonElement labelElement = element.getAsJsonObject().remove(typeFieldName);
				if (labelElement == null) throw new JsonParseException("");
				String name = labelElement.getAsString();
				Class<?> subclass = nameToSubclass.get(name);
				if (subclass == null) throw new JsonParseException("cannot find " + name + " subclass of " + baseType);
				return (R) subclassToAdapter.get(subclass).fromJsonTree(element);
			}
		};
	}
}
