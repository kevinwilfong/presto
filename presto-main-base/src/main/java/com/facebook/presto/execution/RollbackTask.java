/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.execution;

import com.facebook.presto.Session;
import com.facebook.presto.common.transaction.TransactionId;
import com.facebook.presto.metadata.Metadata;
import com.facebook.presto.spi.PrestoException;
import com.facebook.presto.spi.security.AccessControl;
import com.facebook.presto.sql.tree.Expression;
import com.facebook.presto.sql.tree.Rollback;
import com.facebook.presto.transaction.TransactionManager;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

import static com.facebook.presto.spi.StandardErrorCode.NOT_IN_TRANSACTION;
import static com.google.common.util.concurrent.Futures.immediateFuture;

public class RollbackTask
        implements SessionTransactionControlTask<Rollback>
{
    @Override
    public String getName()
    {
        return "ROLLBACK";
    }

    @Override
    public ListenableFuture<?> execute(Rollback statement, TransactionManager transactionManager, Metadata metadata, AccessControl accessControl, QueryStateMachine stateMachine, List<Expression> parameters, String query)
    {
        Session session = stateMachine.getSession();
        if (!session.getTransactionId().isPresent()) {
            throw new PrestoException(NOT_IN_TRANSACTION, "No transaction in progress");
        }
        TransactionId transactionId = session.getTransactionId().get();

        stateMachine.clearTransactionId();
        transactionManager.asyncAbort(transactionId);
        return immediateFuture(null);
    }
}
