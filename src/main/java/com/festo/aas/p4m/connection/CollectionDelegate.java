package com.festo.aas.p4m.connection;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.vab.modelprovider.lambda.VABLambdaHandler;

public class CollectionDelegate extends ValueDelegate<Collection<ISubmodelElement>> {
    @Override
    public void setGetHandler(Supplier<Collection<ISubmodelElement>> getHandler) {
        Supplier<Map<String, ISubmodelElement>> supplier = () -> {
            Collection<ISubmodelElement> collection = getHandler.get();
            return collection.stream().collect(Collectors.toMap(sme -> sme.getIdShort(), sme -> sme));
        };

        lambdaMap.put(VABLambdaHandler.VALUE_GET_SUFFIX, supplier);
    }

    @Override
    public void setSetHandler(Consumer<Collection<ISubmodelElement>> setHandler) {
        Consumer<Map<String, ISubmodelElement>> consumer = (map) -> {
            setHandler.accept(map.values());
        };

        lambdaMap.put(VABLambdaHandler.VALUE_SET_SUFFIX, consumer);
    }
}
