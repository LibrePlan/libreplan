package org.navalplanner.business.common;


public interface IAdHocTransactionService {

    public <T> T onTransaction(IOnTransaction<T> onTransaction);

    public <T> T onReadOnlyTransaction(IOnTransaction<T> onTransaction);

}
