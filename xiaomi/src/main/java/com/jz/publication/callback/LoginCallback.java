package com.jz.publication.callback;

public interface LoginCallback {
    void onSuccess(String result);

    void onFailure(String msg, int code);
}
