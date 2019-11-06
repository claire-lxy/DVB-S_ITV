package com.konkawise.dtv.rx;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RxTransformer {

    /**
     * RxJava使用compose转换，每个RxJava处理链不需要再重复调用subscribeOn()和observeOn()
     *
     * Observable.just(1, 2, 3).compose(RxTransformers.threadTransformer()).subscribe();
     */
    public static <T> ObservableTransformer<T, T> threadTransformer() {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                return upstream.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }
}
