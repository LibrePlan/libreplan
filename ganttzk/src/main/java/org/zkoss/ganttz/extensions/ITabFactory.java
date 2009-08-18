package org.zkoss.ganttz.extensions;

public interface ITabFactory<T> {

    public ITab create(IContext<T> context);

}
