package pl.hanusik.pawel.pcstatus;

public interface Callback<R> {
    void onComplete(R result);
}
