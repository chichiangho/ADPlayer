package com.chichiangho.common.extentions

import android.arch.lifecycle.LifecycleOwner
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.ObservableSubscribeProxy
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import io.reactivex.Emitter
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

fun <T> Emitter<T>.onNextComplete(t: T) {
    onNext(t)
    onComplete()
}

fun <T> Observable<T>.io_main(): Observable<T> =
        subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

fun <T> Observable<T>.autoDispose(owner: LifecycleOwner): ObservableSubscribeProxy<T> =
        `as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(owner)))

fun <T> Observable<T>.autoDispose_io_main(owner: LifecycleOwner): ObservableSubscribeProxy<T> =
        io_main().autoDispose(owner)

fun <T> Observable<ArrayList<T>>.flat(): Observable<T> = flatMap { Observable.fromIterable(it) }

fun delayThenRunOnUiThread(millisecond: Long, owner: LifecycleOwner? = null, action: () -> Unit): Disposable {
    val observable = Observable.timer(millisecond, TimeUnit.MILLISECONDS).io_main()
    return owner?.let {
        observable.autoDispose(it).subscribe { action.invoke() }
    } ?: observable.subscribe { action.invoke() }
}

fun intervalUntilSuccess(millisecond: Long, immediately: Boolean = false, owner: LifecycleOwner? = null, getDisposableAction: ((disposable: Disposable) -> Unit)? = null, invokeAction: (count: Int) -> Boolean) {
    val observer = Observable.interval(if (immediately) 0 else millisecond, millisecond, TimeUnit.MILLISECONDS)
    if (owner != null)
        observer.autoDispose(owner)
    observer.subscribe(object : Observer<Long> {
        lateinit var dispose: Disposable
        var count = 0
        override fun onComplete() {
        }

        override fun onSubscribe(d: Disposable) {
            dispose = d
            getDisposableAction?.invoke(dispose)
        }

        override fun onNext(t: Long) {
            if (invokeAction.invoke(count++) && !dispose.isDisposed)
                dispose.dispose()
        }

        override fun onError(e: Throwable) {
        }
    })
}

fun intervalUntilSuccessOnMain(millisecond: Long, immediately: Boolean = false, owner: LifecycleOwner? = null, getDisposableAction: ((disposable: Disposable) -> Unit)? = null, invokeAction: (count: Int) -> Boolean) {
    val observer = Observable.interval(if (immediately) 0 else millisecond, millisecond, TimeUnit.MILLISECONDS).io_main()
    if (owner != null)
        observer.autoDispose(owner)
    observer.subscribe(object : Observer<Long> {
        lateinit var dispose: Disposable
        var count = 0
        override fun onComplete() {
        }

        override fun onSubscribe(d: Disposable) {
            dispose = d
            getDisposableAction?.invoke(dispose)
        }

        override fun onNext(t: Long) {
            if (invokeAction.invoke(count++) && !dispose.isDisposed)
                dispose.dispose()
        }

        override fun onError(e: Throwable) {
        }
    })
}

fun <T> rxDoInBackground(action: () -> T): Observable<T> =
        Observable.create<T> {
            try {
                it.onNext(action.invoke())
            } catch (e: Exception) {
                it.onError(e)
            }
        }.subscribeOn(Schedulers.io())

fun <T> runOnUiThread(action: () -> T) =
        rxRunOnUiThread(action).commit()

fun <T> rxRunOnUiThread(action: () -> T): Observable<T> =
        Observable.create<T> {
            try {
                it.onNext(action.invoke())
            } catch (e: Exception) {
                it.onError(e)
            }
        }.subscribeOn(AndroidSchedulers.mainThread())

fun <T, R> Observable<T>.rxRunOnUiThread(action: (it: T) -> R): Observable<R> =
        observeOn(AndroidSchedulers.mainThread()).map { action.invoke(it) }

fun <T, R> Observable<T>.rxDoInBackground(action: (it: T) -> R): Observable<R> =
        observeOn(Schedulers.io()).map { action.invoke(it) }

//如果action内有异步，一定要用Async
fun <T> rxAsyncDoInBackground(action: (emitter: ObservableEmitter<T>) -> T): Observable<T> =
        Observable.create<T> {
            try {
                action.invoke(it)
            } catch (e: Exception) {
                it.onError(e)
            }
        }.subscribeOn(Schedulers.io())

fun <T> rxAsyncRunOnUiThread(action: (emitter: ObservableEmitter<T>) -> T): Observable<T> =
        Observable.create<T> {
            try {
                action.invoke(it)
            } catch (e: Exception) {
                it.onError(e)
            }
        }.subscribeOn(AndroidSchedulers.mainThread())

fun <T, R> Observable<T>.rxAsyncRunOnUiThread(action: (it: T, emitter: ObservableEmitter<R>) -> R): Observable<R> =
        flatMap { data ->
            Observable.create<R> {
                try {
                    action.invoke(data, it)
                } catch (e: Exception) {
                    it.onError(e)
                }
            }.subscribeOn(AndroidSchedulers.mainThread())
        }

fun <T, R> Observable<T>.rxAsyncDoInBackground(action: (it: T, emitter: ObservableEmitter<R>) -> R): Observable<R> =
        flatMap { data ->
            Observable.create<R> {
                try {
                    action.invoke(data, it)
                } catch (e: Exception) {
                    it.onError(e)
                }
            }.subscribeOn(Schedulers.io())
        }

fun <T> Observable<T>.commit(lifecycleOwner: LifecycleOwner? = null, error: ((e: Throwable) -> T)? = null) {
    lifecycleOwner?.let {
        autoDispose(it)
    }
    subscribe(object : Observer<T> {
        lateinit var dispose: Disposable
        override fun onComplete() {
        }

        override fun onSubscribe(d: Disposable) {
            dispose = d
        }

        override fun onNext(t: T) {
            if (!dispose.isDisposed)
                dispose.dispose()
        }

        override fun onError(e: Throwable) {
            error?.invoke(e)
        }
    })
}

fun <T> Observable<T>.subscribeOnUiThread(lifecycleOwner: LifecycleOwner? = null, success: ((t: T) -> Unit)? = null, error: ((e: Throwable) -> Unit)? = null) {
    lifecycleOwner?.let {
        autoDispose(it)
    }
    observeOn(AndroidSchedulers.mainThread()).subscribe(object : Observer<T> {
        lateinit var dispose: Disposable
        override fun onComplete() {
        }

        override fun onSubscribe(d: Disposable) {
            dispose = d
        }

        override fun onNext(t: T) {
            success?.invoke(t)
            if (!dispose.isDisposed)
                dispose.dispose()
        }

        override fun onError(e: Throwable) {
            error?.invoke(e)
        }
    })
}
