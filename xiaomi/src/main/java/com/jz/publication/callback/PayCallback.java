package com.jz.publication.callback;

public interface PayCallback {
    void onSuccess(String result);

    void onFailure(String msg, int code);
}