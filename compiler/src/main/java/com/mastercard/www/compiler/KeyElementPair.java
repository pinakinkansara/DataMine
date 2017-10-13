package com.mastercard.www.compiler;

import javax.lang.model.element.Element;

public class KeyElementPair {

    final String key;
    final Element mElement;

    public KeyElementPair(String key, Element element) {
        this.key = key;
        mElement = element;
    }
}
