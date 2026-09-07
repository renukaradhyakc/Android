package com.thelinkphone.app.broadcase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.Intent;

import com.thelinkphone.app.MyAppClass;
import com.thelinkphone.app.service.CallManager;
import com.thelinkphone.app.utils.MyConst;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

public class MyCallReceiverTest {

    private MyCallReceiver receiver;
    private Context mockContext;
    private CallManager mockCallManager;
    private MockedStatic<CallManager> callManagerStatic;

    @Before
    public void setUp() {
        // MyConst computes ACCEPT_CALL/DECLINE_CALL from MyAppClass.myContext
        // the FIRST time MyConst is touched by any code. In the real app that's
        // already set by the time anything runs. In a test, nothing sets it —
        // so we set it ourselves here, before any test body references MyConst.
        MyAppClass.myContext = mock(Context.class);
        when(MyAppClass.myContext.getPackageName()).thenReturn("com.thelinkphone.app");

        receiver = new MyCallReceiver();

        // A mocked Context - a fake stand-in object we can inspect afterwards
        // (e.g. "was startActivity() called on this?").
        mockContext = mock(Context.class);

        // CallManager.getInstance() is a real singleton. We don't want the real
        // one (it touches real telecom state). mockStatic() intercepts the
        // static method call itself and hands back our fake instead.
        mockCallManager = mock(CallManager.class);
        callManagerStatic = mockStatic(CallManager.class);
        callManagerStatic.when(CallManager::getInstance).thenReturn(mockCallManager);
    }

    @After
    public void tearDown() {
        // Static mocks are global while active - must be closed after each test
        // or they'll leak into other tests and cause confusing failures.
        callManagerStatic.close();
    }

    // Helper: build a fake Intent that returns a specific action string,
    // instead of relying on the real (non-functional, in tests) Intent class.
    private Intent intentWithAction(String action) {
        Intent intent = mock(Intent.class);
        when(intent.getAction()).thenReturn(action);
        return intent;
    }

    @Test
    public void onReceive_withNullContext_doesNothing() {
        Intent intent = intentWithAction(MyConst.ACCEPT_CALL);
        receiver.onReceive(null, intent);

        verify(mockCallManager, never()).accept();
        verify(mockCallManager, never()).reject();
    }

    @Test
    public void onReceive_withNullIntent_doesNothing() {
        receiver.onReceive(mockContext, null);

        verify(mockCallManager, never()).accept();
        verify(mockCallManager, never()).reject();
    }

    @Test
    public void onReceive_withNullAction_doesNothing() {
        Intent intent = intentWithAction(null);
        receiver.onReceive(mockContext, intent);

        verify(mockCallManager, never()).accept();
        verify(mockCallManager, never()).reject();
    }

    @Test
    public void onReceive_acceptAction_callsAcceptAndStartsActivity() {
        Intent intent = intentWithAction(MyConst.ACCEPT_CALL);
        receiver.onReceive(mockContext, intent);

        verify(mockCallManager, times(1)).accept();
        verify(mockCallManager, never()).reject();
        verify(mockContext, times(1)).startActivity(any(Intent.class));
    }

    @Test
    public void onReceive_declineAction_callsReject() {
        Intent intent = intentWithAction(MyConst.DECLINE_CALL);
        receiver.onReceive(mockContext, intent);

        verify(mockCallManager, times(1)).reject();
        verify(mockCallManager, never()).accept();
    }

    @Test
    public void onReceive_unknownAction_fallsThroughToReject() {
        // Locks in the ACTUAL current behavior: any action that isn't
        // ACCEPT_CALL - including a garbage/unrelated action - falls through
        // to reject(). Flagged earlier as worth confirming is intended.
        Intent intent = intentWithAction("some.random.action");
        receiver.onReceive(mockContext, intent);

        verify(mockCallManager, times(1)).reject();
    }
}