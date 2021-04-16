package com.jz.publication.callback;

public interface VerifyCallback {
    void onSuccess(String result);

    void onFailure(String msg, int code);
}
