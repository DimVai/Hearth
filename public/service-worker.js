'use strict';



//********************      BASIC VANILLA SERVICE WORKER      //********************

// import Workbox
self.importScripts('https://storage.googleapis.com/workbox-cdn/releases/7.4.1/workbox-sw.js');

// disable console logs
workbox.setConfig({ debug: false });   

// skipWaiting: activate the new version of service worker now, instead of waiting for the next session to do so
self.addEventListener('install', event => { self.skipWaiting() });

// notify when the new updated service worker (this file) gets activated
self.addEventListener('activate', event => { 
    event.waitUntil(self.clients.claim());  // να εφαρμοστεί και στις ανοιχτές σελίδες, όχι μόνο όταν ανοίξουν ξανά
    console.debug('service worker activated', event);
});



//********************            CACHING STRATEGY            //********************

const _PAGES_CACHE = 'hearth-pages-v1';
const _ASSETS_CACHE = 'hearth-assets-v1';
const _EXTERNAL_ASSETS_CACHE = 'hearth-external-assets-v1';

/** Αποθηκεύει μόνο το pathname ως κλειδί cache, χωρίς τα query parameters */
function useOnlyPathname() {
    return {
        cacheKeyWillBeUsed: async ({ request }) => {
            const url = new URL(request.url);
            url.search = '';
            return url.href;
        }
    };
}

// Prefer the network for same-origin PAGES, then fall back to a cached HTML shell.
// Navigation requests are cached by pathname so routes like edit-connection.html?id=abc
// and edit-connection.html?id=xyz reuse the same offline page shell: edit-connection.html
workbox.routing.registerRoute(
    ({ request, sameOrigin }) => sameOrigin && request.mode === 'navigate',
    new workbox.strategies.StaleWhileRevalidate({
        cacheName: _PAGES_CACHE,
        networkTimeoutSeconds: 3,
        plugins: [useOnlyPathname()],
    }),
);

// For other same-origin ASSETS (non-pages), keep exact request URLs as cache keys.
workbox.routing.registerRoute(
    ({ sameOrigin }) => sameOrigin,
    new workbox.strategies.StaleWhileRevalidate({
        cacheName: _ASSETS_CACHE,
        networkTimeoutSeconds: 3,
    }),
);

// For all other requests (typically cross-origin assets), serve stale first
// and refresh the cache in the background after a successful network response.
workbox.routing.registerRoute(
    ({ sameOrigin }) => !sameOrigin,
    new workbox.strategies.StaleWhileRevalidate({
        cacheName: _EXTERNAL_ASSETS_CACHE,
        networkTimeoutSeconds: 3,
    }),
);



//********************       LOCAL PUSH NOTIFICATIONS        //********************
self.importScripts('./pwa/sw-notifications.js');