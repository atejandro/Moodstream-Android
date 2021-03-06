/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * This code was generated by https://code.google.com/p/google-apis-client-generator/
 * (build: 2013-10-30 15:57:41 UTC)
 * on 2013-10-31 at 01:31:37 UTC 
 * Modify at your own risk.
 */

package com.moodstream.model.photoendpoint;

/**
 * Service definition for Photoendpoint (v1).
 *
 * <p>
 * This is an API
 * </p>
 *
 * <p>
 * For more information about this service, see the
 * <a href="" target="_blank">API Documentation</a>
 * </p>
 *
 * <p>
 * This service uses {@link PhotoendpointRequestInitializer} to initialize global parameters via its
 * {@link Builder}.
 * </p>
 *
 * @since 1.3
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public class Photoendpoint extends com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient {

  // Note: Leave this static initializer at the top of the file.
  static {
    com.google.api.client.util.Preconditions.checkState(
        com.google.api.client.googleapis.GoogleUtils.MAJOR_VERSION == 1 &&
        com.google.api.client.googleapis.GoogleUtils.MINOR_VERSION >= 15,
        "You are currently running with version %s of google-api-client. " +
        "You need at least version 1.15 of google-api-client to run version " +
        "1.15.0-rc of the photoendpoint library.", com.google.api.client.googleapis.GoogleUtils.VERSION);
  }

  /**
   * The default encoded root URL of the service. This is determined when the library is generated
   * and normally should not be changed.
   *
   * @since 1.7
   */
  public static final String DEFAULT_ROOT_URL = "https://moodstreamers.appspot.com/_ah/api/";

  /**
   * The default encoded service path of the service. This is determined when the library is
   * generated and normally should not be changed.
   *
   * @since 1.7
   */
  public static final String DEFAULT_SERVICE_PATH = "photoendpoint/v1/";

  /**
   * The default encoded base URL of the service. This is determined when the library is generated
   * and normally should not be changed.
   */
  public static final String DEFAULT_BASE_URL = DEFAULT_ROOT_URL + DEFAULT_SERVICE_PATH;

  /**
   * Constructor.
   *
   * <p>
   * Use {@link Builder} if you need to specify any of the optional parameters.
   * </p>
   *
   * @param transport HTTP transport, which should normally be:
   *        <ul>
   *        <li>Google App Engine:
   *        {@code com.google.api.client.extensions.appengine.http.UrlFetchTransport}</li>
   *        <li>Android: {@code newCompatibleTransport} from
   *        {@code com.google.api.client.extensions.android.http.AndroidHttp}</li>
   *        <li>Java: {@link com.google.api.client.googleapis.javanet.GoogleNetHttpTransport#newTrustedTransport()}
   *        </li>
   *        </ul>
   * @param jsonFactory JSON factory, which may be:
   *        <ul>
   *        <li>Jackson: {@code com.google.api.client.json.jackson2.JacksonFactory}</li>
   *        <li>Google GSON: {@code com.google.api.client.json.gson.GsonFactory}</li>
   *        <li>Android Honeycomb or higher:
   *        {@code com.google.api.client.extensions.android.json.AndroidJsonFactory}</li>
   *        </ul>
   * @param httpRequestInitializer HTTP request initializer or {@code null} for none
   * @since 1.7
   */
  public Photoendpoint(com.google.api.client.http.HttpTransport transport, com.google.api.client.json.JsonFactory jsonFactory,
      com.google.api.client.http.HttpRequestInitializer httpRequestInitializer) {
    this(new Builder(transport, jsonFactory, httpRequestInitializer));
  }

  /**
   * @param builder builder
   */
  Photoendpoint(Builder builder) {
    super(builder);
  }

  @Override
  protected void initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest<?> httpClientRequest) throws java.io.IOException {
    super.initialize(httpClientRequest);
  }

  /**
   * Create a request for the method "getPhoto".
   *
   * This request holds the parameters needed by the the photoendpoint server.  After setting any
   * optional parameters, call the {@link GetPhoto#execute()} method to invoke the remote operation.
   *
   * @param id
   * @return the request
   */
  public GetPhoto getPhoto(java.lang.String id) throws java.io.IOException {
    GetPhoto result = new GetPhoto(id);
    initialize(result);
    return result;
  }

  public class GetPhoto extends PhotoendpointRequest<com.moodstream.model.photoendpoint.model.Photo> {

    private static final String REST_PATH = "photo/{id}";

    /**
     * Create a request for the method "getPhoto".
     *
     * This request holds the parameters needed by the the photoendpoint server.  After setting any
     * optional parameters, call the {@link GetPhoto#execute()} method to invoke the remote operation.
     * <p> {@link
     * GetPhoto#initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest)}
     * must be called to initialize this instance immediately after invoking the constructor. </p>
     *
     * @param id
     * @since 1.13
     */
    protected GetPhoto(java.lang.String id) {
      super(Photoendpoint.this, "GET", REST_PATH, null, com.moodstream.model.photoendpoint.model.Photo.class);
      this.id = com.google.api.client.util.Preconditions.checkNotNull(id, "Required parameter id must be specified.");
    }

    @Override
    public com.google.api.client.http.HttpResponse executeUsingHead() throws java.io.IOException {
      return super.executeUsingHead();
    }

    @Override
    public com.google.api.client.http.HttpRequest buildHttpRequestUsingHead() throws java.io.IOException {
      return super.buildHttpRequestUsingHead();
    }

    @Override
    public GetPhoto setAlt(java.lang.String alt) {
      return (GetPhoto) super.setAlt(alt);
    }

    @Override
    public GetPhoto setFields(java.lang.String fields) {
      return (GetPhoto) super.setFields(fields);
    }

    @Override
    public GetPhoto setKey(java.lang.String key) {
      return (GetPhoto) super.setKey(key);
    }

    @Override
    public GetPhoto setOauthToken(java.lang.String oauthToken) {
      return (GetPhoto) super.setOauthToken(oauthToken);
    }

    @Override
    public GetPhoto setPrettyPrint(java.lang.Boolean prettyPrint) {
      return (GetPhoto) super.setPrettyPrint(prettyPrint);
    }

    @Override
    public GetPhoto setQuotaUser(java.lang.String quotaUser) {
      return (GetPhoto) super.setQuotaUser(quotaUser);
    }

    @Override
    public GetPhoto setUserIp(java.lang.String userIp) {
      return (GetPhoto) super.setUserIp(userIp);
    }

    @com.google.api.client.util.Key
    private java.lang.String id;

    /**

     */
    public java.lang.String getId() {
      return id;
    }

    public GetPhoto setId(java.lang.String id) {
      this.id = id;
      return this;
    }

    @Override
    public GetPhoto set(String parameterName, Object value) {
      return (GetPhoto) super.set(parameterName, value);
    }
  }

  /**
   * Create a request for the method "insertPhoto".
   *
   * This request holds the parameters needed by the the photoendpoint server.  After setting any
   * optional parameters, call the {@link InsertPhoto#execute()} method to invoke the remote
   * operation.
   *
   * @param content the {@link com.moodstream.model.photoendpoint.model.Photo}
   * @return the request
   */
  public InsertPhoto insertPhoto(com.moodstream.model.photoendpoint.model.Photo content) throws java.io.IOException {
    InsertPhoto result = new InsertPhoto(content);
    initialize(result);
    return result;
  }

  public class InsertPhoto extends PhotoendpointRequest<com.moodstream.model.photoendpoint.model.Photo> {

    private static final String REST_PATH = "photo";

    /**
     * Create a request for the method "insertPhoto".
     *
     * This request holds the parameters needed by the the photoendpoint server.  After setting any
     * optional parameters, call the {@link InsertPhoto#execute()} method to invoke the remote
     * operation. <p> {@link
     * InsertPhoto#initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest)}
     * must be called to initialize this instance immediately after invoking the constructor. </p>
     *
     * @param content the {@link com.moodstream.model.photoendpoint.model.Photo}
     * @since 1.13
     */
    protected InsertPhoto(com.moodstream.model.photoendpoint.model.Photo content) {
      super(Photoendpoint.this, "POST", REST_PATH, content, com.moodstream.model.photoendpoint.model.Photo.class);
    }

    @Override
    public InsertPhoto setAlt(java.lang.String alt) {
      return (InsertPhoto) super.setAlt(alt);
    }

    @Override
    public InsertPhoto setFields(java.lang.String fields) {
      return (InsertPhoto) super.setFields(fields);
    }

    @Override
    public InsertPhoto setKey(java.lang.String key) {
      return (InsertPhoto) super.setKey(key);
    }

    @Override
    public InsertPhoto setOauthToken(java.lang.String oauthToken) {
      return (InsertPhoto) super.setOauthToken(oauthToken);
    }

    @Override
    public InsertPhoto setPrettyPrint(java.lang.Boolean prettyPrint) {
      return (InsertPhoto) super.setPrettyPrint(prettyPrint);
    }

    @Override
    public InsertPhoto setQuotaUser(java.lang.String quotaUser) {
      return (InsertPhoto) super.setQuotaUser(quotaUser);
    }

    @Override
    public InsertPhoto setUserIp(java.lang.String userIp) {
      return (InsertPhoto) super.setUserIp(userIp);
    }

    @Override
    public InsertPhoto set(String parameterName, Object value) {
      return (InsertPhoto) super.set(parameterName, value);
    }
  }

  /**
   * Create a request for the method "listPhoto".
   *
   * This request holds the parameters needed by the the photoendpoint server.  After setting any
   * optional parameters, call the {@link ListPhoto#execute()} method to invoke the remote operation.
   *
   * @return the request
   */
  public ListPhoto listPhoto() throws java.io.IOException {
    ListPhoto result = new ListPhoto();
    initialize(result);
    return result;
  }

  public class ListPhoto extends PhotoendpointRequest<com.moodstream.model.photoendpoint.model.CollectionResponsePhoto> {

    private static final String REST_PATH = "photo";

    /**
     * Create a request for the method "listPhoto".
     *
     * This request holds the parameters needed by the the photoendpoint server.  After setting any
     * optional parameters, call the {@link ListPhoto#execute()} method to invoke the remote
     * operation. <p> {@link
     * ListPhoto#initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest)}
     * must be called to initialize this instance immediately after invoking the constructor. </p>
     *
     * @since 1.13
     */
    protected ListPhoto() {
      super(Photoendpoint.this, "GET", REST_PATH, null, com.moodstream.model.photoendpoint.model.CollectionResponsePhoto.class);
    }

    @Override
    public com.google.api.client.http.HttpResponse executeUsingHead() throws java.io.IOException {
      return super.executeUsingHead();
    }

    @Override
    public com.google.api.client.http.HttpRequest buildHttpRequestUsingHead() throws java.io.IOException {
      return super.buildHttpRequestUsingHead();
    }

    @Override
    public ListPhoto setAlt(java.lang.String alt) {
      return (ListPhoto) super.setAlt(alt);
    }

    @Override
    public ListPhoto setFields(java.lang.String fields) {
      return (ListPhoto) super.setFields(fields);
    }

    @Override
    public ListPhoto setKey(java.lang.String key) {
      return (ListPhoto) super.setKey(key);
    }

    @Override
    public ListPhoto setOauthToken(java.lang.String oauthToken) {
      return (ListPhoto) super.setOauthToken(oauthToken);
    }

    @Override
    public ListPhoto setPrettyPrint(java.lang.Boolean prettyPrint) {
      return (ListPhoto) super.setPrettyPrint(prettyPrint);
    }

    @Override
    public ListPhoto setQuotaUser(java.lang.String quotaUser) {
      return (ListPhoto) super.setQuotaUser(quotaUser);
    }

    @Override
    public ListPhoto setUserIp(java.lang.String userIp) {
      return (ListPhoto) super.setUserIp(userIp);
    }

    @com.google.api.client.util.Key
    private java.lang.String cursor;

    /**

     */
    public java.lang.String getCursor() {
      return cursor;
    }

    public ListPhoto setCursor(java.lang.String cursor) {
      this.cursor = cursor;
      return this;
    }

    @com.google.api.client.util.Key
    private java.lang.Long eventId;

    /**

     */
    public java.lang.Long getEventId() {
      return eventId;
    }

    public ListPhoto setEventId(java.lang.Long eventId) {
      this.eventId = eventId;
      return this;
    }

    @com.google.api.client.util.Key
    private java.lang.Integer limit;

    /**

     */
    public java.lang.Integer getLimit() {
      return limit;
    }

    public ListPhoto setLimit(java.lang.Integer limit) {
      this.limit = limit;
      return this;
    }

    @Override
    public ListPhoto set(String parameterName, Object value) {
      return (ListPhoto) super.set(parameterName, value);
    }
  }

  /**
   * Create a request for the method "removePhoto".
   *
   * This request holds the parameters needed by the the photoendpoint server.  After setting any
   * optional parameters, call the {@link RemovePhoto#execute()} method to invoke the remote
   * operation.
   *
   * @param id
   * @return the request
   */
  public RemovePhoto removePhoto(java.lang.String id) throws java.io.IOException {
    RemovePhoto result = new RemovePhoto(id);
    initialize(result);
    return result;
  }

  public class RemovePhoto extends PhotoendpointRequest<Void> {

    private static final String REST_PATH = "photo/{id}";

    /**
     * Create a request for the method "removePhoto".
     *
     * This request holds the parameters needed by the the photoendpoint server.  After setting any
     * optional parameters, call the {@link RemovePhoto#execute()} method to invoke the remote
     * operation. <p> {@link
     * RemovePhoto#initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest)}
     * must be called to initialize this instance immediately after invoking the constructor. </p>
     *
     * @param id
     * @since 1.13
     */
    protected RemovePhoto(java.lang.String id) {
      super(Photoendpoint.this, "DELETE", REST_PATH, null, Void.class);
      this.id = com.google.api.client.util.Preconditions.checkNotNull(id, "Required parameter id must be specified.");
    }

    @Override
    public RemovePhoto setAlt(java.lang.String alt) {
      return (RemovePhoto) super.setAlt(alt);
    }

    @Override
    public RemovePhoto setFields(java.lang.String fields) {
      return (RemovePhoto) super.setFields(fields);
    }

    @Override
    public RemovePhoto setKey(java.lang.String key) {
      return (RemovePhoto) super.setKey(key);
    }

    @Override
    public RemovePhoto setOauthToken(java.lang.String oauthToken) {
      return (RemovePhoto) super.setOauthToken(oauthToken);
    }

    @Override
    public RemovePhoto setPrettyPrint(java.lang.Boolean prettyPrint) {
      return (RemovePhoto) super.setPrettyPrint(prettyPrint);
    }

    @Override
    public RemovePhoto setQuotaUser(java.lang.String quotaUser) {
      return (RemovePhoto) super.setQuotaUser(quotaUser);
    }

    @Override
    public RemovePhoto setUserIp(java.lang.String userIp) {
      return (RemovePhoto) super.setUserIp(userIp);
    }

    @com.google.api.client.util.Key
    private java.lang.String id;

    /**

     */
    public java.lang.String getId() {
      return id;
    }

    public RemovePhoto setId(java.lang.String id) {
      this.id = id;
      return this;
    }

    @Override
    public RemovePhoto set(String parameterName, Object value) {
      return (RemovePhoto) super.set(parameterName, value);
    }
  }

  /**
   * Create a request for the method "updatePhoto".
   *
   * This request holds the parameters needed by the the photoendpoint server.  After setting any
   * optional parameters, call the {@link UpdatePhoto#execute()} method to invoke the remote
   * operation.
   *
   * @param content the {@link com.moodstream.model.photoendpoint.model.Photo}
   * @return the request
   */
  public UpdatePhoto updatePhoto(com.moodstream.model.photoendpoint.model.Photo content) throws java.io.IOException {
    UpdatePhoto result = new UpdatePhoto(content);
    initialize(result);
    return result;
  }

  public class UpdatePhoto extends PhotoendpointRequest<com.moodstream.model.photoendpoint.model.Photo> {

    private static final String REST_PATH = "photo";

    /**
     * Create a request for the method "updatePhoto".
     *
     * This request holds the parameters needed by the the photoendpoint server.  After setting any
     * optional parameters, call the {@link UpdatePhoto#execute()} method to invoke the remote
     * operation. <p> {@link
     * UpdatePhoto#initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest)}
     * must be called to initialize this instance immediately after invoking the constructor. </p>
     *
     * @param content the {@link com.moodstream.model.photoendpoint.model.Photo}
     * @since 1.13
     */
    protected UpdatePhoto(com.moodstream.model.photoendpoint.model.Photo content) {
      super(Photoendpoint.this, "PUT", REST_PATH, content, com.moodstream.model.photoendpoint.model.Photo.class);
    }

    @Override
    public UpdatePhoto setAlt(java.lang.String alt) {
      return (UpdatePhoto) super.setAlt(alt);
    }

    @Override
    public UpdatePhoto setFields(java.lang.String fields) {
      return (UpdatePhoto) super.setFields(fields);
    }

    @Override
    public UpdatePhoto setKey(java.lang.String key) {
      return (UpdatePhoto) super.setKey(key);
    }

    @Override
    public UpdatePhoto setOauthToken(java.lang.String oauthToken) {
      return (UpdatePhoto) super.setOauthToken(oauthToken);
    }

    @Override
    public UpdatePhoto setPrettyPrint(java.lang.Boolean prettyPrint) {
      return (UpdatePhoto) super.setPrettyPrint(prettyPrint);
    }

    @Override
    public UpdatePhoto setQuotaUser(java.lang.String quotaUser) {
      return (UpdatePhoto) super.setQuotaUser(quotaUser);
    }

    @Override
    public UpdatePhoto setUserIp(java.lang.String userIp) {
      return (UpdatePhoto) super.setUserIp(userIp);
    }

    @Override
    public UpdatePhoto set(String parameterName, Object value) {
      return (UpdatePhoto) super.set(parameterName, value);
    }
  }

  /**
   * Builder for {@link Photoendpoint}.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   *
   * @since 1.3.0
   */
  public static final class Builder extends com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient.Builder {

    /**
     * Returns an instance of a new builder.
     *
     * @param transport HTTP transport, which should normally be:
     *        <ul>
     *        <li>Google App Engine:
     *        {@code com.google.api.client.extensions.appengine.http.UrlFetchTransport}</li>
     *        <li>Android: {@code newCompatibleTransport} from
     *        {@code com.google.api.client.extensions.android.http.AndroidHttp}</li>
     *        <li>Java: {@link com.google.api.client.googleapis.javanet.GoogleNetHttpTransport#newTrustedTransport()}
     *        </li>
     *        </ul>
     * @param jsonFactory JSON factory, which may be:
     *        <ul>
     *        <li>Jackson: {@code com.google.api.client.json.jackson2.JacksonFactory}</li>
     *        <li>Google GSON: {@code com.google.api.client.json.gson.GsonFactory}</li>
     *        <li>Android Honeycomb or higher:
     *        {@code com.google.api.client.extensions.android.json.AndroidJsonFactory}</li>
     *        </ul>
     * @param httpRequestInitializer HTTP request initializer or {@code null} for none
     * @since 1.7
     */
    public Builder(com.google.api.client.http.HttpTransport transport, com.google.api.client.json.JsonFactory jsonFactory,
        com.google.api.client.http.HttpRequestInitializer httpRequestInitializer) {
      super(
          transport,
          jsonFactory,
          DEFAULT_ROOT_URL,
          DEFAULT_SERVICE_PATH,
          httpRequestInitializer,
          false);
    }

    /** Builds a new instance of {@link Photoendpoint}. */
    @Override
    public Photoendpoint build() {
      return new Photoendpoint(this);
    }

    @Override
    public Builder setRootUrl(String rootUrl) {
      return (Builder) super.setRootUrl(rootUrl);
    }

    @Override
    public Builder setServicePath(String servicePath) {
      return (Builder) super.setServicePath(servicePath);
    }

    @Override
    public Builder setHttpRequestInitializer(com.google.api.client.http.HttpRequestInitializer httpRequestInitializer) {
      return (Builder) super.setHttpRequestInitializer(httpRequestInitializer);
    }

    @Override
    public Builder setApplicationName(String applicationName) {
      return (Builder) super.setApplicationName(applicationName);
    }

    @Override
    public Builder setSuppressPatternChecks(boolean suppressPatternChecks) {
      return (Builder) super.setSuppressPatternChecks(suppressPatternChecks);
    }

    @Override
    public Builder setSuppressRequiredParameterChecks(boolean suppressRequiredParameterChecks) {
      return (Builder) super.setSuppressRequiredParameterChecks(suppressRequiredParameterChecks);
    }

    @Override
    public Builder setSuppressAllChecks(boolean suppressAllChecks) {
      return (Builder) super.setSuppressAllChecks(suppressAllChecks);
    }

    /**
     * Set the {@link PhotoendpointRequestInitializer}.
     *
     * @since 1.12
     */
    public Builder setPhotoendpointRequestInitializer(
        PhotoendpointRequestInitializer photoendpointRequestInitializer) {
      return (Builder) super.setGoogleClientRequestInitializer(photoendpointRequestInitializer);
    }

    @Override
    public Builder setGoogleClientRequestInitializer(
        com.google.api.client.googleapis.services.GoogleClientRequestInitializer googleClientRequestInitializer) {
      return (Builder) super.setGoogleClientRequestInitializer(googleClientRequestInitializer);
    }
  }
}
