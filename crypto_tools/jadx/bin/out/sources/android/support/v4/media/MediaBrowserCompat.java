package android.support.v4.media;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.BundleCompat;
import android.support.v4.media.MediaBrowserCompatApi21;
import android.support.v4.media.MediaBrowserCompatApi23;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/* loaded from: classes.dex */
public final class MediaBrowserCompat {
    public static final String EXTRA_PAGE = "android.media.browse.extra.PAGE";
    public static final String EXTRA_PAGE_SIZE = "android.media.browse.extra.PAGE_SIZE";
    private static final String TAG = "MediaBrowserCompat";
    private final MediaBrowserImpl mImpl;

    /* loaded from: classes.dex */
    interface MediaBrowserImpl {
        void connect();

        void disconnect();

        @Nullable
        Bundle getExtras();

        void getItem(@NonNull String str, @NonNull ItemCallback itemCallback);

        @NonNull
        String getRoot();

        ComponentName getServiceComponent();

        @NonNull
        MediaSessionCompat.Token getSessionToken();

        boolean isConnected();

        void subscribe(@NonNull String str, Bundle bundle, @NonNull SubscriptionCallback subscriptionCallback);

        void unsubscribe(@NonNull String str, Bundle bundle);
    }

    /* loaded from: classes.dex */
    interface MediaBrowserServiceCallbackImpl {
        void onConnectionFailed(Messenger messenger);

        void onLoadChildren(Messenger messenger, String str, List list, Bundle bundle);

        void onServiceConnected(Messenger messenger, String str, MediaSessionCompat.Token token, Bundle bundle);
    }

    public MediaBrowserCompat(Context context, ComponentName serviceComponent, ConnectionCallback callback, Bundle rootHints) {
        if (Build.VERSION.SDK_INT >= 23) {
            this.mImpl = new MediaBrowserImplApi23(context, serviceComponent, callback, rootHints);
        } else if (Build.VERSION.SDK_INT >= 21) {
            this.mImpl = new MediaBrowserImplApi21(context, serviceComponent, callback, rootHints);
        } else {
            this.mImpl = new MediaBrowserImplBase(context, serviceComponent, callback, rootHints);
        }
    }

    public void connect() {
        this.mImpl.connect();
    }

    public void disconnect() {
        this.mImpl.disconnect();
    }

    public boolean isConnected() {
        return this.mImpl.isConnected();
    }

    @NonNull
    public ComponentName getServiceComponent() {
        return this.mImpl.getServiceComponent();
    }

    @NonNull
    public String getRoot() {
        return this.mImpl.getRoot();
    }

    @Nullable
    public Bundle getExtras() {
        return this.mImpl.getExtras();
    }

    @NonNull
    public MediaSessionCompat.Token getSessionToken() {
        return this.mImpl.getSessionToken();
    }

    public void subscribe(@NonNull String parentId, @NonNull SubscriptionCallback callback) {
        this.mImpl.subscribe(parentId, null, callback);
    }

    public void subscribe(@NonNull String parentId, @NonNull Bundle options, @NonNull SubscriptionCallback callback) {
        if (options == null) {
            throw new IllegalArgumentException("options are null");
        }
        this.mImpl.subscribe(parentId, options, callback);
    }

    public void unsubscribe(@NonNull String parentId) {
        this.mImpl.unsubscribe(parentId, null);
    }

    public void unsubscribe(@NonNull String parentId, @NonNull Bundle options) {
        if (options == null) {
            throw new IllegalArgumentException("options are null");
        }
        this.mImpl.unsubscribe(parentId, options);
    }

    public void getItem(@NonNull String mediaId, @NonNull ItemCallback cb) {
        this.mImpl.getItem(mediaId, cb);
    }

    /* loaded from: classes.dex */
    public static class MediaItem implements Parcelable {
        public static final Parcelable.Creator<MediaItem> CREATOR = new Parcelable.Creator<MediaItem>() { // from class: android.support.v4.media.MediaBrowserCompat.MediaItem.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public MediaItem createFromParcel(Parcel in) {
                return new MediaItem(in);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public MediaItem[] newArray(int size) {
                return new MediaItem[size];
            }
        };
        public static final int FLAG_BROWSABLE = 1;
        public static final int FLAG_PLAYABLE = 2;
        private final MediaDescriptionCompat mDescription;
        private final int mFlags;

        @Retention(RetentionPolicy.SOURCE)
        /* loaded from: classes.dex */
        public @interface Flags {
        }

        public MediaItem(@NonNull MediaDescriptionCompat description, int flags) {
            if (description == null) {
                throw new IllegalArgumentException("description cannot be null");
            }
            if (TextUtils.isEmpty(description.getMediaId())) {
                throw new IllegalArgumentException("description must have a non-empty media id");
            }
            this.mFlags = flags;
            this.mDescription = description;
        }

        private MediaItem(Parcel in) {
            this.mFlags = in.readInt();
            this.mDescription = MediaDescriptionCompat.CREATOR.createFromParcel(in);
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(this.mFlags);
            this.mDescription.writeToParcel(out, flags);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("MediaItem{");
            sb.append("mFlags=").append(this.mFlags);
            sb.append(", mDescription=").append(this.mDescription);
            sb.append('}');
            return sb.toString();
        }

        public int getFlags() {
            return this.mFlags;
        }

        public boolean isBrowsable() {
            return (this.mFlags & 1) != 0;
        }

        public boolean isPlayable() {
            return (this.mFlags & 2) != 0;
        }

        @NonNull
        public MediaDescriptionCompat getDescription() {
            return this.mDescription;
        }

        @NonNull
        public String getMediaId() {
            return this.mDescription.getMediaId();
        }
    }

    /* loaded from: classes.dex */
    public static class ConnectionCallback {
        private ConnectionCallbackInternal mConnectionCallbackInternal;
        final Object mConnectionCallbackObj;

        /* JADX INFO: Access modifiers changed from: package-private */
        /* loaded from: classes.dex */
        public interface ConnectionCallbackInternal {
            void onConnected();

            void onConnectionFailed();

            void onConnectionSuspended();
        }

        public ConnectionCallback() {
            if (Build.VERSION.SDK_INT >= 21) {
                this.mConnectionCallbackObj = MediaBrowserCompatApi21.createConnectionCallback(new StubApi21());
            } else {
                this.mConnectionCallbackObj = null;
            }
        }

        public void onConnected() {
        }

        public void onConnectionSuspended() {
        }

        public void onConnectionFailed() {
        }

        void setInternalConnectionCallback(ConnectionCallbackInternal connectionCallbackInternal) {
            this.mConnectionCallbackInternal = connectionCallbackInternal;
        }

        /* loaded from: classes.dex */
        private class StubApi21 implements MediaBrowserCompatApi21.ConnectionCallback {
            private StubApi21() {
            }

            @Override // android.support.v4.media.MediaBrowserCompatApi21.ConnectionCallback
            public void onConnected() {
                if (ConnectionCallback.this.mConnectionCallbackInternal != null) {
                    ConnectionCallback.this.mConnectionCallbackInternal.onConnected();
                }
                ConnectionCallback.this.onConnected();
            }

            @Override // android.support.v4.media.MediaBrowserCompatApi21.ConnectionCallback
            public void onConnectionSuspended() {
                if (ConnectionCallback.this.mConnectionCallbackInternal != null) {
                    ConnectionCallback.this.mConnectionCallbackInternal.onConnectionSuspended();
                }
                ConnectionCallback.this.onConnectionSuspended();
            }

            @Override // android.support.v4.media.MediaBrowserCompatApi21.ConnectionCallback
            public void onConnectionFailed() {
                if (ConnectionCallback.this.mConnectionCallbackInternal != null) {
                    ConnectionCallback.this.mConnectionCallbackInternal.onConnectionFailed();
                }
                ConnectionCallback.this.onConnectionFailed();
            }
        }
    }

    /* loaded from: classes.dex */
    public static abstract class SubscriptionCallback {
        public void onChildrenLoaded(@NonNull String parentId, List<MediaItem> children) {
        }

        public void onChildrenLoaded(@NonNull String parentId, List<MediaItem> children, @NonNull Bundle options) {
        }

        public void onError(@NonNull String parentId) {
        }

        public void onError(@NonNull String parentId, @NonNull Bundle options) {
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class SubscriptionCallbackApi21 extends SubscriptionCallback {
        private Bundle mOptions;
        SubscriptionCallback mSubscriptionCallback;
        private final Object mSubscriptionCallbackObj = MediaBrowserCompatApi21.createSubscriptionCallback(new StubApi21());

        public SubscriptionCallbackApi21(SubscriptionCallback callback, Bundle options) {
            this.mSubscriptionCallback = callback;
            this.mOptions = options;
        }

        @Override // android.support.v4.media.MediaBrowserCompat.SubscriptionCallback
        public void onChildrenLoaded(@NonNull String parentId, List<MediaItem> children) {
            this.mSubscriptionCallback.onChildrenLoaded(parentId, children);
        }

        @Override // android.support.v4.media.MediaBrowserCompat.SubscriptionCallback
        public void onChildrenLoaded(@NonNull String parentId, List<MediaItem> children, @NonNull Bundle options) {
            this.mSubscriptionCallback.onChildrenLoaded(parentId, children, options);
        }

        @Override // android.support.v4.media.MediaBrowserCompat.SubscriptionCallback
        public void onError(@NonNull String parentId) {
            this.mSubscriptionCallback.onError(parentId);
        }

        @Override // android.support.v4.media.MediaBrowserCompat.SubscriptionCallback
        public void onError(@NonNull String parentId, @NonNull Bundle options) {
            this.mSubscriptionCallback.onError(parentId, options);
        }

        /* loaded from: classes.dex */
        private class StubApi21 implements MediaBrowserCompatApi21.SubscriptionCallback {
            private StubApi21() {
            }

            @Override // android.support.v4.media.MediaBrowserCompatApi21.SubscriptionCallback
            public void onChildrenLoaded(@NonNull String parentId, List<Parcel> children) {
                List<MediaItem> mediaItems = null;
                if (children != null) {
                    mediaItems = new ArrayList<>();
                    for (Parcel parcel : children) {
                        parcel.setDataPosition(0);
                        mediaItems.add(MediaItem.CREATOR.createFromParcel(parcel));
                        parcel.recycle();
                    }
                }
                if (SubscriptionCallbackApi21.this.mOptions != null) {
                    SubscriptionCallbackApi21.this.onChildrenLoaded(parentId, MediaBrowserCompatUtils.applyOptions(mediaItems, SubscriptionCallbackApi21.this.mOptions), SubscriptionCallbackApi21.this.mOptions);
                } else {
                    SubscriptionCallbackApi21.this.onChildrenLoaded(parentId, mediaItems);
                }
            }

            @Override // android.support.v4.media.MediaBrowserCompatApi21.SubscriptionCallback
            public void onError(@NonNull String parentId) {
                if (SubscriptionCallbackApi21.this.mOptions != null) {
                    SubscriptionCallbackApi21.this.onError(parentId, SubscriptionCallbackApi21.this.mOptions);
                } else {
                    SubscriptionCallbackApi21.this.onError(parentId);
                }
            }
        }
    }

    /* loaded from: classes.dex */
    public static abstract class ItemCallback {
        final Object mItemCallbackObj;

        public ItemCallback() {
            if (Build.VERSION.SDK_INT >= 23) {
                this.mItemCallbackObj = MediaBrowserCompatApi23.createItemCallback(new StubApi23());
            } else {
                this.mItemCallbackObj = null;
            }
        }

        public void onItemLoaded(MediaItem item) {
        }

        public void onError(@NonNull String itemId) {
        }

        /* loaded from: classes.dex */
        private class StubApi23 implements MediaBrowserCompatApi23.ItemCallback {
            private StubApi23() {
            }

            @Override // android.support.v4.media.MediaBrowserCompatApi23.ItemCallback
            public void onItemLoaded(Parcel itemParcel) {
                itemParcel.setDataPosition(0);
                MediaItem item = MediaItem.CREATOR.createFromParcel(itemParcel);
                itemParcel.recycle();
                ItemCallback.this.onItemLoaded(item);
            }

            @Override // android.support.v4.media.MediaBrowserCompatApi23.ItemCallback
            public void onError(@NonNull String itemId) {
                ItemCallback.this.onError(itemId);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class MediaBrowserImplBase implements MediaBrowserImpl, MediaBrowserServiceCallbackImpl {
        private static final int CONNECT_STATE_CONNECTED = 2;
        private static final int CONNECT_STATE_CONNECTING = 1;
        private static final int CONNECT_STATE_DISCONNECTED = 0;
        private static final int CONNECT_STATE_SUSPENDED = 3;
        private static final boolean DBG = false;
        private final ConnectionCallback mCallback;
        private Messenger mCallbacksMessenger;
        private final Context mContext;
        private Bundle mExtras;
        private MediaSessionCompat.Token mMediaSessionToken;
        private final Bundle mRootHints;
        private String mRootId;
        private ServiceBinderWrapper mServiceBinderWrapper;
        private final ComponentName mServiceComponent;
        private MediaServiceConnection mServiceConnection;
        private final CallbackHandler mHandler = new CallbackHandler(this);
        private final ArrayMap<String, Subscription> mSubscriptions = new ArrayMap<>();
        private int mState = 0;

        public MediaBrowserImplBase(Context context, ComponentName serviceComponent, ConnectionCallback callback, Bundle rootHints) {
            if (context == null) {
                throw new IllegalArgumentException("context must not be null");
            }
            if (serviceComponent == null) {
                throw new IllegalArgumentException("service component must not be null");
            }
            if (callback == null) {
                throw new IllegalArgumentException("connection callback must not be null");
            }
            this.mContext = context;
            this.mServiceComponent = serviceComponent;
            this.mCallback = callback;
            this.mRootHints = rootHints;
        }

        @Override // android.support.v4.media.MediaBrowserCompat.MediaBrowserImpl
        public void connect() {
            if (this.mState != 0) {
                throw new IllegalStateException("connect() called while not disconnected (state=" + getStateLabel(this.mState) + ")");
            }
            if (this.mServiceBinderWrapper != null) {
                throw new RuntimeException("mServiceBinderWrapper should be null. Instead it is " + this.mServiceBinderWrapper);
            }
            if (this.mCallbacksMessenger != null) {
                throw new RuntimeException("mCallbacksMessenger should be null. Instead it is " + this.mCallbacksMessenger);
            }
            this.mState = 1;
            Intent intent = new Intent(MediaBrowserServiceCompat.SERVICE_INTERFACE);
            intent.setComponent(this.mServiceComponent);
            final MediaServiceConnection mediaServiceConnection = new MediaServiceConnection();
            this.mServiceConnection = mediaServiceConnection;
            boolean bound = false;
            try {
                bound = this.mContext.bindService(intent, this.mServiceConnection, 1);
            } catch (Exception e) {
                Log.e(MediaBrowserCompat.TAG, "Failed binding to service " + this.mServiceComponent);
            }
            if (!bound) {
                this.mHandler.post(new Runnable() { // from class: android.support.v4.media.MediaBrowserCompat.MediaBrowserImplBase.1
                    @Override // java.lang.Runnable
                    public void run() {
                        if (mediaServiceConnection == MediaBrowserImplBase.this.mServiceConnection) {
                            MediaBrowserImplBase.this.forceCloseConnection();
                            MediaBrowserImplBase.this.mCallback.onConnectionFailed();
                        }
                    }
                });
            }
        }

        @Override // android.support.v4.media.MediaBrowserCompat.MediaBrowserImpl
        public void disconnect() {
            if (this.mCallbacksMessenger != null) {
                try {
                    this.mServiceBinderWrapper.disconnect(this.mCallbacksMessenger);
                } catch (RemoteException e) {
                    Log.w(MediaBrowserCompat.TAG, "RemoteException during connect for " + this.mServiceComponent);
                }
            }
            forceCloseConnection();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void forceCloseConnection() {
            if (this.mServiceConnection != null) {
                this.mContext.unbindService(this.mServiceConnection);
            }
            this.mState = 0;
            this.mServiceConnection = null;
            this.mServiceBinderWrapper = null;
            this.mCallbacksMessenger = null;
            this.mHandler.setCallbacksMessenger(null);
            this.mRootId = null;
            this.mMediaSessionToken = null;
        }

        @Override // android.support.v4.media.MediaBrowserCompat.MediaBrowserImpl
        public boolean isConnected() {
            return this.mState == 2;
        }

        @Override // android.support.v4.media.MediaBrowserCompat.MediaBrowserImpl
        @NonNull
        public ComponentName getServiceComponent() {
            if (!isConnected()) {
                throw new IllegalStateException("getServiceComponent() called while not connected (state=" + this.mState + ")");
            }
            return this.mServiceComponent;
        }

        @Override // android.support.v4.media.MediaBrowserCompat.MediaBrowserImpl
        @NonNull
        public String getRoot() {
            if (!isConnected()) {
                throw new IllegalStateException("getRoot() called while not connected(state=" + getStateLabel(this.mState) + ")");
            }
            return this.mRootId;
        }

        @Override // android.support.v4.media.MediaBrowserCompat.MediaBrowserImpl
        @Nullable
        public Bundle getExtras() {
            if (!isConnected()) {
                throw new IllegalStateException("getExtras() called while not connected (state=" + getStateLabel(this.mState) + ")");
            }
            return this.mExtras;
        }

        @Override // android.support.v4.media.MediaBrowserCompat.MediaBrowserImpl
        @NonNull
        public MediaSessionCompat.Token getSessionToken() {
            if (!isConnected()) {
                throw new IllegalStateException("getSessionToken() called while not connected(state=" + this.mState + ")");
            }
            return this.mMediaSessionToken;
        }

        @Override // android.support.v4.media.MediaBrowserCompat.MediaBrowserImpl
        public void subscribe(@NonNull String parentId, Bundle options, @NonNull SubscriptionCallback callback) {
            if (TextUtils.isEmpty(parentId)) {
                throw new IllegalArgumentException("parentId is empty.");
            }
            if (callback == null) {
                throw new IllegalArgumentException("callback is null");
            }
            Subscription sub = this.mSubscriptions.get(parentId);
            if (sub == null) {
                sub = new Subscription();
                this.mSubscriptions.put(parentId, sub);
            }
            sub.setCallbackForOptions(callback, options);
            if (this.mState == 2) {
                try {
                    this.mServiceBinderWrapper.addSubscription(parentId, options, this.mCallbacksMessenger);
                } catch (RemoteException e) {
                    Log.d(MediaBrowserCompat.TAG, "addSubscription failed with RemoteException parentId=" + parentId);
                }
            }
        }

        @Override // android.support.v4.media.MediaBrowserCompat.MediaBrowserImpl
        public void unsubscribe(@NonNull String parentId, Bundle options) {
            if (TextUtils.isEmpty(parentId)) {
                throw new IllegalArgumentException("parentId is empty.");
            }
            Subscription sub = this.mSubscriptions.get(parentId);
            if (sub != null && sub.remove(options) && this.mState == 2) {
                try {
                    this.mServiceBinderWrapper.removeSubscription(parentId, options, this.mCallbacksMessenger);
                } catch (RemoteException e) {
                    Log.d(MediaBrowserCompat.TAG, "removeSubscription failed with RemoteException parentId=" + parentId);
                }
            }
            if (sub != null && sub.isEmpty()) {
                this.mSubscriptions.remove(parentId);
            }
        }

        @Override // android.support.v4.media.MediaBrowserCompat.MediaBrowserImpl
        public void getItem(@NonNull final String mediaId, @NonNull final ItemCallback cb) {
            if (TextUtils.isEmpty(mediaId)) {
                throw new IllegalArgumentException("mediaId is empty.");
            }
            if (cb == null) {
                throw new IllegalArgumentException("cb is null.");
            }
            if (this.mState != 2) {
                Log.i(MediaBrowserCompat.TAG, "Not connected, unable to retrieve the MediaItem.");
                this.mHandler.post(new Runnable() { // from class: android.support.v4.media.MediaBrowserCompat.MediaBrowserImplBase.2
                    @Override // java.lang.Runnable
                    public void run() {
                        cb.onError(mediaId);
                    }
                });
                return;
            }
            ResultReceiver receiver = new ItemReceiver(mediaId, cb, this.mHandler);
            try {
                this.mServiceBinderWrapper.getMediaItem(mediaId, receiver);
            } catch (RemoteException e) {
                Log.i(MediaBrowserCompat.TAG, "Remote error getting media item.");
                this.mHandler.post(new Runnable() { // from class: android.support.v4.media.MediaBrowserCompat.MediaBrowserImplBase.3
                    @Override // java.lang.Runnable
                    public void run() {
                        cb.onError(mediaId);
                    }
                });
            }
        }

        @Override // android.support.v4.media.MediaBrowserCompat.MediaBrowserServiceCallbackImpl
        public void onServiceConnected(Messenger callback, String root, MediaSessionCompat.Token session, Bundle extra) {
            if (isCurrent(callback, "onConnect")) {
                if (this.mState != 1) {
                    Log.w(MediaBrowserCompat.TAG, "onConnect from service while mState=" + getStateLabel(this.mState) + "... ignoring");
                    return;
                }
                this.mRootId = root;
                this.mMediaSessionToken = session;
                this.mExtras = extra;
                this.mState = 2;
                this.mCallback.onConnected();
                try {
                    for (Map.Entry<String, Subscription> subscriptionEntry : this.mSubscriptions.entrySet()) {
                        String id = subscriptionEntry.getKey();
                        Subscription sub = subscriptionEntry.getValue();
                        for (Bundle options : sub.getOptionsList()) {
                            this.mServiceBinderWrapper.addSubscription(id, options, this.mCallbacksMessenger);
                        }
                    }
                } catch (RemoteException e) {
                    Log.d(MediaBrowserCompat.TAG, "addSubscription failed with RemoteException.");
                }
            }
        }

        @Override // android.support.v4.media.MediaBrowserCompat.MediaBrowserServiceCallbackImpl
        public void onConnectionFailed(Messenger callback) {
            Log.e(MediaBrowserCompat.TAG, "onConnectFailed for " + this.mServiceComponent);
            if (isCurrent(callback, "onConnectFailed")) {
                if (this.mState != 1) {
                    Log.w(MediaBrowserCompat.TAG, "onConnect from service while mState=" + getStateLabel(this.mState) + "... ignoring");
                    return;
                }
                forceCloseConnection();
                this.mCallback.onConnectionFailed();
            }
        }

        @Override // android.support.v4.media.MediaBrowserCompat.MediaBrowserServiceCallbackImpl
        public void onLoadChildren(Messenger callback, String parentId, List list, Bundle options) {
            Subscription subscription;
            SubscriptionCallback subscriptionCallback;
            if (isCurrent(callback, "onLoadChildren") && (subscription = this.mSubscriptions.get(parentId)) != null && (subscriptionCallback = subscription.getCallback(options)) != null) {
                if (options == null) {
                    subscriptionCallback.onChildrenLoaded(parentId, list);
                } else {
                    subscriptionCallback.onChildrenLoaded(parentId, list, options);
                }
            }
        }

        private static String getStateLabel(int state) {
            switch (state) {
                case 0:
                    return "CONNECT_STATE_DISCONNECTED";
                case 1:
                    return "CONNECT_STATE_CONNECTING";
                case 2:
                    return "CONNECT_STATE_CONNECTED";
                case 3:
                    return "CONNECT_STATE_SUSPENDED";
                default:
                    return "UNKNOWN/" + state;
            }
        }

        private boolean isCurrent(Messenger callback, String funcName) {
            if (this.mCallbacksMessenger != callback) {
                if (this.mState != 0) {
                    Log.i(MediaBrowserCompat.TAG, funcName + " for " + this.mServiceComponent + " with mCallbacksMessenger=" + this.mCallbacksMessenger + " this=" + this);
                }
                return false;
            }
            return true;
        }

        void dump() {
            Log.d(MediaBrowserCompat.TAG, "MediaBrowserCompat...");
            Log.d(MediaBrowserCompat.TAG, "  mServiceComponent=" + this.mServiceComponent);
            Log.d(MediaBrowserCompat.TAG, "  mCallback=" + this.mCallback);
            Log.d(MediaBrowserCompat.TAG, "  mRootHints=" + this.mRootHints);
            Log.d(MediaBrowserCompat.TAG, "  mState=" + getStateLabel(this.mState));
            Log.d(MediaBrowserCompat.TAG, "  mServiceConnection=" + this.mServiceConnection);
            Log.d(MediaBrowserCompat.TAG, "  mServiceBinderWrapper=" + this.mServiceBinderWrapper);
            Log.d(MediaBrowserCompat.TAG, "  mCallbacksMessenger=" + this.mCallbacksMessenger);
            Log.d(MediaBrowserCompat.TAG, "  mRootId=" + this.mRootId);
            Log.d(MediaBrowserCompat.TAG, "  mMediaSessionToken=" + this.mMediaSessionToken);
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* loaded from: classes.dex */
        public class MediaServiceConnection implements ServiceConnection {
            private MediaServiceConnection() {
            }

            @Override // android.content.ServiceConnection
            public void onServiceConnected(final ComponentName name, final IBinder binder) {
                postOrRun(new Runnable() { // from class: android.support.v4.media.MediaBrowserCompat.MediaBrowserImplBase.MediaServiceConnection.1
                    @Override // java.lang.Runnable
                    public void run() {
                        if (MediaServiceConnection.this.isCurrent("onServiceConnected")) {
                            MediaBrowserImplBase.this.mServiceBinderWrapper = new ServiceBinderWrapper(binder);
                            MediaBrowserImplBase.this.mCallbacksMessenger = new Messenger(MediaBrowserImplBase.this.mHandler);
                            MediaBrowserImplBase.this.mHandler.setCallbacksMessenger(MediaBrowserImplBase.this.mCallbacksMessenger);
                            MediaBrowserImplBase.this.mState = 1;
                            try {
                                MediaBrowserImplBase.this.mServiceBinderWrapper.connect(MediaBrowserImplBase.this.mContext, MediaBrowserImplBase.this.mRootHints, MediaBrowserImplBase.this.mCallbacksMessenger);
                            } catch (RemoteException e) {
                                Log.w(MediaBrowserCompat.TAG, "RemoteException during connect for " + MediaBrowserImplBase.this.mServiceComponent);
                            }
                        }
                    }
                });
            }

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(final ComponentName name) {
                postOrRun(new Runnable() { // from class: android.support.v4.media.MediaBrowserCompat.MediaBrowserImplBase.MediaServiceConnection.2
                    @Override // java.lang.Runnable
                    public void run() {
                        if (MediaServiceConnection.this.isCurrent("onServiceDisconnected")) {
                            MediaBrowserImplBase.this.mServiceBinderWrapper = null;
                            MediaBrowserImplBase.this.mCallbacksMessenger = null;
                            MediaBrowserImplBase.this.mHandler.setCallbacksMessenger(null);
                            MediaBrowserImplBase.this.mState = 3;
                            MediaBrowserImplBase.this.mCallback.onConnectionSuspended();
                        }
                    }
                });
            }

            private void postOrRun(Runnable r) {
                if (Thread.currentThread() != MediaBrowserImplBase.this.mHandler.getLooper().getThread()) {
                    MediaBrowserImplBase.this.mHandler.post(r);
                } else {
                    r.run();
                }
            }

            /* JADX INFO: Access modifiers changed from: private */
            public boolean isCurrent(String funcName) {
                if (MediaBrowserImplBase.this.mServiceConnection != this) {
                    if (MediaBrowserImplBase.this.mState != 0) {
                        Log.i(MediaBrowserCompat.TAG, funcName + " for " + MediaBrowserImplBase.this.mServiceComponent + " with mServiceConnection=" + MediaBrowserImplBase.this.mServiceConnection + " this=" + this);
                    }
                    return false;
                }
                return true;
            }
        }
    }

    /* loaded from: classes.dex */
    static class MediaBrowserImplApi21 implements MediaBrowserImpl, MediaBrowserServiceCallbackImpl, ConnectionCallback.ConnectionCallbackInternal {
        private static final boolean DBG = false;
        protected Object mBrowserObj;
        private Messenger mCallbacksMessenger;
        private ServiceBinderWrapper mServiceBinderWrapper;
        private final ComponentName mServiceComponent;
        private final CallbackHandler mHandler = new CallbackHandler(this);
        private final ArrayMap<String, Subscription> mSubscriptions = new ArrayMap<>();

        public MediaBrowserImplApi21(Context context, ComponentName serviceComponent, ConnectionCallback callback, Bundle rootHints) {
            this.mServiceComponent = serviceComponent;
            callback.setInternalConnectionCallback(this);
            this.mBrowserObj = MediaBrowserCompatApi21.createBrowser(context, serviceComponent, callback.mConnectionCallbackObj, rootHints);
        }

        @Override // android.support.v4.media.MediaBrowserCompat.MediaBrowserImpl
        public void connect() {
            MediaBrowserCompatApi21.connect(this.mBrowserObj);
        }

        @Override // android.support.v4.media.MediaBrowserCompat.MediaBrowserImpl
        public void disconnect() {
            if (this.mServiceBinderWrapper != null && this.mCallbacksMessenger != null) {
                try {
                    this.mServiceBinderWrapper.unregisterCallbackMessenger(this.mCallbacksMessenger);
                } catch (RemoteException e) {
                    Log.i(MediaBrowserCompat.TAG, "Remote error unregistering client messenger.");
                }
            }
            MediaBrowserCompatApi21.disconnect(this.mBrowserObj);
        }

        @Override // android.support.v4.media.MediaBrowserCompat.MediaBrowserImpl
        public boolean isConnected() {
            return MediaBrowserCompatApi21.isConnected(this.mBrowserObj);
        }

        @Override // android.support.v4.media.MediaBrowserCompat.MediaBrowserImpl
        public ComponentName getServiceComponent() {
            return MediaBrowserCompatApi21.getServiceComponent(this.mBrowserObj);
        }

        @Override // android.support.v4.media.MediaBrowserCompat.MediaBrowserImpl
        @NonNull
        public String getRoot() {
            return MediaBrowserCompatApi21.getRoot(this.mBrowserObj);
        }

        @Override // android.support.v4.media.MediaBrowserCompat.MediaBrowserImpl
        @Nullable
        public Bundle getExtras() {
            return MediaBrowserCompatApi21.getExtras(this.mBrowserObj);
        }

        @Override // android.support.v4.media.MediaBrowserCompat.MediaBrowserImpl
        @NonNull
        public MediaSessionCompat.Token getSessionToken() {
            return MediaSessionCompat.Token.fromToken(MediaBrowserCompatApi21.getSessionToken(this.mBrowserObj));
        }

        @Override // android.support.v4.media.MediaBrowserCompat.MediaBrowserImpl
        public void subscribe(@NonNull String parentId, Bundle options, @NonNull SubscriptionCallback callback) {
            SubscriptionCallbackApi21 cb21 = new SubscriptionCallbackApi21(callback, options);
            Subscription sub = this.mSubscriptions.get(parentId);
            if (sub == null) {
                sub = new Subscription();
                this.mSubscriptions.put(parentId, sub);
            }
            sub.setCallbackForOptions(cb21, options);
            if (MediaBrowserCompatApi21.isConnected(this.mBrowserObj)) {
                if (options == null || this.mServiceBinderWrapper == null) {
                    MediaBrowserCompatApi21.subscribe(this.mBrowserObj, parentId, cb21.mSubscriptionCallbackObj);
                    return;
                }
                try {
                    this.mServiceBinderWrapper.addSubscription(parentId, options, this.mCallbacksMessenger);
                } catch (RemoteException e) {
                    Log.i(MediaBrowserCompat.TAG, "Remote error subscribing media item: " + parentId);
                }
            }
        }

        @Override // android.support.v4.media.MediaBrowserCompat.MediaBrowserImpl
        public void unsubscribe(@NonNull String parentId, Bundle options) {
            if (TextUtils.isEmpty(parentId)) {
                throw new IllegalArgumentException("parentId is empty.");
            }
            Subscription sub = this.mSubscriptions.get(parentId);
            if (sub != null && sub.remove(options)) {
                if (options == null || this.mServiceBinderWrapper == null) {
                    if (this.mServiceBinderWrapper != null || sub.isEmpty()) {
                        MediaBrowserCompatApi21.unsubscribe(this.mBrowserObj, parentId);
                    }
                } else if (this.mServiceBinderWrapper == null) {
                    try {
                        this.mServiceBinderWrapper.removeSubscription(parentId, options, this.mCallbacksMessenger);
                    } catch (RemoteException e) {
                        Log.d(MediaBrowserCompat.TAG, "removeSubscription failed with RemoteException parentId=" + parentId);
                    }
                }
            }
            if (sub != null && sub.isEmpty()) {
                this.mSubscriptions.remove(parentId);
            }
        }

        @Override // android.support.v4.media.MediaBrowserCompat.MediaBrowserImpl
        public void getItem(@NonNull final String mediaId, @NonNull final ItemCallback cb) {
            if (TextUtils.isEmpty(mediaId)) {
                throw new IllegalArgumentException("mediaId is empty.");
            }
            if (cb == null) {
                throw new IllegalArgumentException("cb is null.");
            }
            if (!MediaBrowserCompatApi21.isConnected(this.mBrowserObj)) {
                Log.i(MediaBrowserCompat.TAG, "Not connected, unable to retrieve the MediaItem.");
                this.mHandler.post(new Runnable() { // from class: android.support.v4.media.MediaBrowserCompat.MediaBrowserImplApi21.1
                    @Override // java.lang.Runnable
                    public void run() {
                        cb.onError(mediaId);
                    }
                });
            } else if (this.mServiceBinderWrapper == null) {
                this.mHandler.post(new Runnable() { // from class: android.support.v4.media.MediaBrowserCompat.MediaBrowserImplApi21.2
                    @Override // java.lang.Runnable
                    public void run() {
                        cb.onItemLoaded(null);
                    }
                });
            } else {
                ResultReceiver receiver = new ItemReceiver(mediaId, cb, this.mHandler);
                try {
                    this.mServiceBinderWrapper.getMediaItem(mediaId, receiver);
                } catch (RemoteException e) {
                    Log.i(MediaBrowserCompat.TAG, "Remote error getting media item: " + mediaId);
                    this.mHandler.post(new Runnable() { // from class: android.support.v4.media.MediaBrowserCompat.MediaBrowserImplApi21.3
                        @Override // java.lang.Runnable
                        public void run() {
                            cb.onError(mediaId);
                        }
                    });
                }
            }
        }

        @Override // android.support.v4.media.MediaBrowserCompat.ConnectionCallback.ConnectionCallbackInternal
        public void onConnected() {
            IBinder serviceBinder;
            Bundle extras = MediaBrowserCompatApi21.getExtras(this.mBrowserObj);
            if (extras != null && (serviceBinder = BundleCompat.getBinder(extras, MediaBrowserProtocol.EXTRA_MESSENGER_BINDER)) != null) {
                this.mServiceBinderWrapper = new ServiceBinderWrapper(serviceBinder);
                this.mCallbacksMessenger = new Messenger(this.mHandler);
                this.mHandler.setCallbacksMessenger(this.mCallbacksMessenger);
                try {
                    this.mServiceBinderWrapper.registerCallbackMessenger(this.mCallbacksMessenger);
                } catch (RemoteException e) {
                    Log.i(MediaBrowserCompat.TAG, "Remote error registering client messenger.");
                }
                onServiceConnected(this.mCallbacksMessenger, null, null, null);
            }
        }

        @Override // android.support.v4.media.MediaBrowserCompat.ConnectionCallback.ConnectionCallbackInternal
        public void onConnectionSuspended() {
            this.mServiceBinderWrapper = null;
            this.mCallbacksMessenger = null;
            this.mHandler.setCallbacksMessenger(null);
        }

        @Override // android.support.v4.media.MediaBrowserCompat.ConnectionCallback.ConnectionCallbackInternal
        public void onConnectionFailed() {
        }

        @Override // android.support.v4.media.MediaBrowserCompat.MediaBrowserServiceCallbackImpl
        public void onServiceConnected(Messenger callback, String root, MediaSessionCompat.Token session, Bundle extra) {
            for (Map.Entry<String, Subscription> subscriptionEntry : this.mSubscriptions.entrySet()) {
                String id = subscriptionEntry.getKey();
                Subscription sub = subscriptionEntry.getValue();
                List<Bundle> optionsList = sub.getOptionsList();
                List<SubscriptionCallback> callbackList = sub.getCallbacks();
                for (int i = 0; i < optionsList.size(); i++) {
                    if (optionsList.get(i) == null) {
                        MediaBrowserCompatApi21.subscribe(this.mBrowserObj, id, ((SubscriptionCallbackApi21) callbackList.get(i)).mSubscriptionCallbackObj);
                    } else {
                        try {
                            this.mServiceBinderWrapper.addSubscription(id, optionsList.get(i), this.mCallbacksMessenger);
                        } catch (RemoteException e) {
                            Log.d(MediaBrowserCompat.TAG, "addSubscription failed with RemoteException parentId=" + id);
                        }
                    }
                }
            }
        }

        @Override // android.support.v4.media.MediaBrowserCompat.MediaBrowserServiceCallbackImpl
        public void onConnectionFailed(Messenger callback) {
        }

        @Override // android.support.v4.media.MediaBrowserCompat.MediaBrowserServiceCallbackImpl
        public void onLoadChildren(Messenger callback, String parentId, List list, @NonNull Bundle options) {
            Subscription subscription;
            if (this.mCallbacksMessenger == callback && (subscription = this.mSubscriptions.get(parentId)) != null) {
                subscription.getCallback(options).onChildrenLoaded(parentId, list, options);
            }
        }
    }

    /* loaded from: classes.dex */
    static class MediaBrowserImplApi23 extends MediaBrowserImplApi21 {
        public MediaBrowserImplApi23(Context context, ComponentName serviceComponent, ConnectionCallback callback, Bundle rootHints) {
            super(context, serviceComponent, callback, rootHints);
        }

        @Override // android.support.v4.media.MediaBrowserCompat.MediaBrowserImplApi21, android.support.v4.media.MediaBrowserCompat.MediaBrowserImpl
        public void getItem(@NonNull String mediaId, @NonNull ItemCallback cb) {
            MediaBrowserCompatApi23.getItem(this.mBrowserObj, mediaId, cb.mItemCallbackObj);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class Subscription {
        private final List<SubscriptionCallback> mCallbacks = new ArrayList();
        private final List<Bundle> mOptionsList = new ArrayList();

        public boolean isEmpty() {
            return this.mCallbacks.isEmpty();
        }

        public List<Bundle> getOptionsList() {
            return this.mOptionsList;
        }

        public List<SubscriptionCallback> getCallbacks() {
            return this.mCallbacks;
        }

        public void setCallbackForOptions(SubscriptionCallback callback, Bundle options) {
            for (int i = 0; i < this.mOptionsList.size(); i++) {
                if (MediaBrowserCompatUtils.areSameOptions(this.mOptionsList.get(i), options)) {
                    this.mCallbacks.set(i, callback);
                    return;
                }
            }
            this.mCallbacks.add(callback);
            this.mOptionsList.add(options);
        }

        public boolean remove(Bundle options) {
            for (int i = 0; i < this.mOptionsList.size(); i++) {
                if (MediaBrowserCompatUtils.areSameOptions(this.mOptionsList.get(i), options)) {
                    this.mCallbacks.remove(i);
                    this.mOptionsList.remove(i);
                    return true;
                }
            }
            return false;
        }

        public SubscriptionCallback getCallback(Bundle options) {
            for (int i = 0; i < this.mOptionsList.size(); i++) {
                if (MediaBrowserCompatUtils.areSameOptions(this.mOptionsList.get(i), options)) {
                    return this.mCallbacks.get(i);
                }
            }
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class CallbackHandler extends Handler {
        private final MediaBrowserServiceCallbackImpl mCallbackImpl;
        private WeakReference<Messenger> mCallbacksMessengerRef;

        CallbackHandler(MediaBrowserServiceCallbackImpl callbackImpl) {
            this.mCallbackImpl = callbackImpl;
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (this.mCallbacksMessengerRef != null) {
                Bundle data = msg.getData();
                data.setClassLoader(MediaSessionCompat.class.getClassLoader());
                switch (msg.what) {
                    case 1:
                        this.mCallbackImpl.onServiceConnected(this.mCallbacksMessengerRef.get(), data.getString(MediaBrowserProtocol.DATA_MEDIA_ITEM_ID), (MediaSessionCompat.Token) data.getParcelable(MediaBrowserProtocol.DATA_MEDIA_SESSION_TOKEN), data.getBundle(MediaBrowserProtocol.DATA_ROOT_HINTS));
                        return;
                    case 2:
                        this.mCallbackImpl.onConnectionFailed(this.mCallbacksMessengerRef.get());
                        return;
                    case 3:
                        this.mCallbackImpl.onLoadChildren(this.mCallbacksMessengerRef.get(), data.getString(MediaBrowserProtocol.DATA_MEDIA_ITEM_ID), data.getParcelableArrayList(MediaBrowserProtocol.DATA_MEDIA_ITEM_LIST), data.getBundle(MediaBrowserProtocol.DATA_OPTIONS));
                        return;
                    default:
                        Log.w(MediaBrowserCompat.TAG, "Unhandled message: " + msg + "\n  Client version: 1\n  Service version: " + msg.arg1);
                        return;
                }
            }
        }

        void setCallbacksMessenger(Messenger callbacksMessenger) {
            this.mCallbacksMessengerRef = new WeakReference<>(callbacksMessenger);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class ServiceBinderWrapper {
        private Messenger mMessenger;

        public ServiceBinderWrapper(IBinder target) {
            this.mMessenger = new Messenger(target);
        }

        void connect(Context context, Bundle rootHint, Messenger callbacksMessenger) throws RemoteException {
            Bundle data = new Bundle();
            data.putString(MediaBrowserProtocol.DATA_PACKAGE_NAME, context.getPackageName());
            data.putBundle(MediaBrowserProtocol.DATA_ROOT_HINTS, rootHint);
            sendRequest(1, data, callbacksMessenger);
        }

        void disconnect(Messenger callbacksMessenger) throws RemoteException {
            sendRequest(2, null, callbacksMessenger);
        }

        void addSubscription(String parentId, Bundle options, Messenger callbacksMessenger) throws RemoteException {
            Bundle data = new Bundle();
            data.putString(MediaBrowserProtocol.DATA_MEDIA_ITEM_ID, parentId);
            data.putBundle(MediaBrowserProtocol.DATA_OPTIONS, options);
            sendRequest(3, data, callbacksMessenger);
        }

        void removeSubscription(String parentId, Bundle options, Messenger callbacksMessenger) throws RemoteException {
            Bundle data = new Bundle();
            data.putString(MediaBrowserProtocol.DATA_MEDIA_ITEM_ID, parentId);
            data.putBundle(MediaBrowserProtocol.DATA_OPTIONS, options);
            sendRequest(4, data, callbacksMessenger);
        }

        void getMediaItem(String mediaId, ResultReceiver receiver) throws RemoteException {
            Bundle data = new Bundle();
            data.putString(MediaBrowserProtocol.DATA_MEDIA_ITEM_ID, mediaId);
            data.putParcelable(MediaBrowserProtocol.DATA_RESULT_RECEIVER, receiver);
            sendRequest(5, data, null);
        }

        void registerCallbackMessenger(Messenger callbackMessenger) throws RemoteException {
            sendRequest(6, null, callbackMessenger);
        }

        void unregisterCallbackMessenger(Messenger callbackMessenger) throws RemoteException {
            sendRequest(7, null, callbackMessenger);
        }

        private void sendRequest(int what, Bundle data, Messenger cbMessenger) throws RemoteException {
            Message msg = Message.obtain();
            msg.what = what;
            msg.arg1 = 1;
            msg.setData(data);
            msg.replyTo = cbMessenger;
            this.mMessenger.send(msg);
        }
    }

    /* loaded from: classes.dex */
    private static class ItemReceiver extends ResultReceiver {
        private final ItemCallback mCallback;
        private final String mMediaId;

        ItemReceiver(String mediaId, ItemCallback callback, Handler handler) {
            super(handler);
            this.mMediaId = mediaId;
            this.mCallback = callback;
        }

        @Override // android.support.v4.os.ResultReceiver
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            resultData.setClassLoader(MediaBrowserCompat.class.getClassLoader());
            if (resultCode != 0 || resultData == null || !resultData.containsKey(MediaBrowserServiceCompat.KEY_MEDIA_ITEM)) {
                this.mCallback.onError(this.mMediaId);
                return;
            }
            Parcelable item = resultData.getParcelable(MediaBrowserServiceCompat.KEY_MEDIA_ITEM);
            if (item instanceof MediaItem) {
                this.mCallback.onItemLoaded((MediaItem) item);
            } else {
                this.mCallback.onError(this.mMediaId);
            }
        }
    }
}
