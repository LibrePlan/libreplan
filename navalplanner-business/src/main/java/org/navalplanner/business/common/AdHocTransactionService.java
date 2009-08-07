package org.navalplanner.business.common;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdHocTransactionService implements IAdHocTransactionService {

    @Override
    @Transactional
    public <T> T onTransaction(IOnTransaction<T> onTransaction) {
        return onTransaction.execute();
    }

    @Override
    @Transactional(readOnly = true)
    public <T> T onReadOnlyTransaction(IOnTransaction<T> onTransaction) {
        return onTransaction.execute();
    }

}
