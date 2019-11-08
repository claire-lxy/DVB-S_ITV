package com.konkawise.dtv.base;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class DisposableDelegate {
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    public void addObservable(Disposable disposable) {
        if (disposable != null) {
            mCompositeDisposable.add(disposable);
        }
    }

    public void removeObservable(Disposable disposable) {
        if (disposable != null) {
            mCompositeDisposable.remove(disposable);
        }
    }

    public void clearObservables() {
        mCompositeDisposable.clear();
    }
}
