/**
 * REST API 공통 Ajax 함수
 * - CSRF 토큰 자동 포함
 * - PUT, DELETE 등 REST 메서드 지원
 */
var BoardAjax = (function () {
  'use strict';

  function getCsrfHeaders(contentType) {
    var meta = document.querySelector('meta[name="_csrf"]');
    var header = document.querySelector('meta[name="_csrf_header"]');
    var headers = { 'Content-Type': contentType || 'application/json' };
    if (meta && header) {
      headers[header.content] = meta.content;
    }
    return headers;
  }

  function buildUrlSearchParams(data) {
    var params = new URLSearchParams();
    Object.keys(data || {}).forEach(function (key) {
      if (data[key] != null) params.append(key, data[key]);
    });
    return params.toString();
  }

  /**
   * REST 요청 (PUT, DELETE, PATCH 등)
   * @param {string} url - 요청 URL
   * @param {string} method - HTTP 메서드 (PUT, DELETE, PATCH)
   * @param {Object} [data] - 데이터 객체 (PUT/PATCH 시)
   * @param {boolean} [asJson=true] - true면 JSON, false면 form-urlencoded
   * @returns {Promise<Response>}
   */
  function request(url, method, data, asJson) {
    if (asJson === undefined) asJson = true;
    var options = {
      method: method,
      headers: getCsrfHeaders(asJson ? 'application/json' : 'application/x-www-form-urlencoded'),
      credentials: 'same-origin'
    };
    if (data && (method === 'PUT' || method === 'PATCH' || method === 'POST')) {
      options.body = asJson ? JSON.stringify(data) : buildUrlSearchParams(data);
    }
    return fetch(url, options);
  }

  /**
   * PUT 요청 (수정)
   * @param {string} url
   * @param {Object} data - JSON 객체
   */
  function put(url, data) {
    return request(url, 'PUT', data, true);
  }

  /**
   * DELETE 요청 (삭제)
   */
  function del(url) {
    return request(url, 'DELETE');
  }

  /**
   * 응답 처리 헬퍼 - JSON 파싱 후 반환
   */
  function handleResponse(response) {
    var contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      return response.json().then(function (body) {
        if (response.ok) return body;
        var err = new Error(body.message || '요청 실패');
        err.body = body;
        err.status = response.status;
        throw err;
      });
    }
    if (response.ok) return Promise.resolve({ success: true });
    return Promise.reject(new Error('요청 실패: ' + response.status));
  }

  return {
    put: put,
    delete: del,
    request: request,
    handleResponse: handleResponse
  };
})();
