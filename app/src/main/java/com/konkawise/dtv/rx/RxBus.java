package com.konkawise.dtv.rx;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class RxBus {
    private final Subject<Object> mEventBus;

    private static final class RxBusHolder {
        private static final RxBus INSTANCE = new RxBus();
    }

    public static RxBus getInstance() {
        return RxBusHolder.INSTANCE;
    }

    private RxBus() {
        mEventBus = PublishSubject.create().toSerialized();
    }

    /**
     * 发送事件
     */
    public void post(Object event) {
        mEventBus.onNext(event);
    }

    /**
     * 接收事件
     */
    public <T> Observable<T> toObservable(final Class<T> eventType) {
        return mEventBus.ofType(eventType);
    }

    /**
     * 是否有订阅者
     */
    public boolean hasObservers() {
        return mEventBus.hasObservers();
    }
}
