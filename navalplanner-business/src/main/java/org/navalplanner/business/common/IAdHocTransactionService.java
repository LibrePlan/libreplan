package org.navalplanner.business.common;


public interface IAdHocTransactionService {

    public <T> T runOnTransaction(IOnTransaction<T> onTransaction);

    public <T> T runOnReadOnlyTransaction(IOnTransaction<T> onTransaction);

}
