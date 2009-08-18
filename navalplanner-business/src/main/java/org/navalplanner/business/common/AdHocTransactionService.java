package org.navalplanner.business.common;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdHocTransactionService implements IAdHocTransactionService {

    @Override
    @Transactional
    public <T> T runOnTransaction(IOnTransaction<T> onTransaction) {
        return onTransaction.execute();
    }

    @Override
    @Transactional(readOnly = true)
    public <T> T runOnReadOnlyTransaction(IOnTransaction<T> onTransaction) {
        return onTransaction.execute();
    }

}
