package shared;

import java.nio.file.Path;
import java.util.function.BiConsumer;

public interface PathSupplier {
	void applyWithAllPath(BiConsumer<Path,String> operator) throws Exception;
}
