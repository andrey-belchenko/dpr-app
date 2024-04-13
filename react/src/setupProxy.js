const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function(app) {
    app.use(
        '/api',
        createProxyMiddleware({
            target: 'http://localhost:8999',
            changeOrigin: true,
        })
    );
    app.use(
        '/ws',
        createProxyMiddleware({
            target: 'http://localhost:8999',
            changeOrigin: true,
        })
    );
    app.use(
        '/keycloak-config',
        createProxyMiddleware({
            target: 'http://localhost:8999',
            changeOrigin: true,
        })
    );
};
