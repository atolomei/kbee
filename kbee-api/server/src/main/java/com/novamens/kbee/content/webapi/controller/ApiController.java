package com.novamens.kbee.content.webapi.controller;

import java.security.cert.Certificate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowService;

import io.swagger.annotations.*;
import kbee.api.model.ApiActivity;
import kbee.api.model.ApiDomain;
import kbee.api.model.ApiFile;
import kbee.api.model.ApiObject;
import kbee.api.model.ApiProcedure;
import kbee.api.model.ApiProxy;
import kbee.api.model.ApiSearch;
import kbee.api.model.ApiValue;
import kbee.api.model.ApiViewMode;
import kbee.api.model.IActivityProxy;
import kbee.api.model.ICertificate;
import kbee.api.model.ApiClassifier;
import kbee.api.model.ICommand;
import kbee.api.model.ApiDataSet;
import kbee.api.model.IDevice;
import kbee.api.model.IEmailTemplate;
import kbee.api.model.IError;
import kbee.api.model.IFacet;
import kbee.api.model.IFieldValue;
import kbee.api.model.IForm;
import kbee.api.model.IFormData;
import kbee.api.model.IGroup;
import kbee.api.model.ILauncher;
import kbee.api.model.ILauncherGroup;
import kbee.api.model.ILibrary;
import kbee.api.model.ILogEvent;
import kbee.api.model.ILoginResponse;
import kbee.api.model.IModelAttribute;
import kbee.api.model.INode;
import kbee.api.model.INote;
import kbee.api.model.IObjectReplica;
import kbee.api.model.IPendingTask;
import kbee.api.model.IPendingTaskProxy;
import kbee.api.model.IPerson;
import kbee.api.model.ApiResource;
import kbee.api.model.IResourceTag;
import kbee.api.model.IResponse;
import kbee.api.model.IRole;
import kbee.api.model.ISecurityRule;
import kbee.api.model.ISettings;
import kbee.api.model.ISignature;
import kbee.api.model.ISignedData;
import kbee.api.model.ISuggestion;
import kbee.api.model.ITemplate;
import kbee.api.model.IToken;
import kbee.api.model.ITransaction;
import kbee.api.model.ApiUser;
import kbee.api.model.IUserDashboard;
import kbee.api.model.IWorkflowEvent;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;
import kbee.api.service.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.extensions.markup.html.repeater.tree.Node;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.base.SecurityRule;
import com.novamens.content.command.Command;
import com.novamens.content.email.EmailTemplate;
import com.novamens.content.entity.Person;
import com.novamens.content.form.EForm;
import com.novamens.content.library.Library;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.LauncherGroup;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.security.Role;
import com.novamens.content.service.DomainService;
import com.novamens.content.service.PersonService;
import com.novamens.content.tree.TreeNode;
import com.novamens.content.user.UserDevice;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.content.user.UserSignature;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.event.LogEvent;
import com.novamens.hibernate.session.Session;
import com.novamens.indexer.iql.ParserException;
import com.novamens.indexer.iql.PredicateNotFoundException;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.query.Suggestion;
import com.novamens.indexer.service.IndexerException;
import com.novamens.kbee.content.document.KbeeIDoc;
import com.novamens.kbee.content.webapi.handler.DeviceUpdateHandler;
import com.novamens.kbee.content.webapi.handler.FileDeleteHandler;
import com.novamens.kbee.content.webapi.handler.FileFormFieldOptionsHandler;
import com.novamens.kbee.content.webapi.handler.FileFormUpdateHandler;
import com.novamens.kbee.content.webapi.handler.FileUpdateAbstractHandler;
import com.novamens.kbee.content.webapi.handler.ProcessLaunchHandler;
import com.novamens.kbee.content.webapi.handler.ReplicaHandler;
import com.novamens.kbee.content.webapi.handler.RequestHandler;
import com.novamens.kbee.content.webapi.handler.SignatureHandler;
import com.novamens.kbee.content.webapi.handler.SignatureUpdateHandler;
import com.novamens.kbee.content.webapi.handler.TakeTaskHandler;
import com.novamens.kbee.content.webapi.handler.UserCreateHandler;
import com.novamens.kbee.content.webapi.handler.UserDashboardHandler;
import com.novamens.kbee.content.webapi.handler.UserDeleteHandler;
import com.novamens.kbee.content.webapi.handler.UserUpdateHandler;
import com.novamens.kbee.content.webapi.handler.ValueCreateHandler;
import com.novamens.kbee.content.webapi.handler.ValueFormFieldOptionsHandler;
import com.novamens.kbee.content.webapi.handler.ValueFormUpdateHandler;
import com.novamens.kbee.content.webapi.handler.ValueUpdateHandler;
import com.novamens.kbee.content.webapi.handler.WorkflowActivityHandler;
import com.novamens.kbee.content.webapi.handler.WorkflowEventHandler;
import com.novamens.kbee.content.webapi.logging.FileDeleteEvent;
import com.novamens.kbee.content.webapi.logging.FileUpdateEvent;
import com.novamens.kbee.content.webapi.logging.ResourceUploadEvent;
import com.novamens.kbee.content.webapi.logging.UserDeleteEvent;
import com.novamens.kbee.content.webapi.logging.UserUpdateEvent;
import com.novamens.kbee.content.webapi.logging.ValueUpdateEvent;
import com.novamens.kbee.content.webapi.logging.WorkflowEvent;
import com.novamens.kbee.content.webapi.traffic.TrafficControlService;
import com.novamens.kbee.content.webapi.traffic.TrafficPass;
import com.novamens.kbee.content.webapi.transaction.ApiTransactionService;
import com.novamens.kbee.content.webapi.type.IModelAttributeAdapter;
import com.novamens.kbee.content.webapi.type.INodeAdapter;
import com.novamens.kbee.content.webapi.type.IProcedureAdapter;
import com.novamens.kbee.content.webapi.type.IProgressNoteAdapter;
import com.novamens.kbee.content.webapi.type.ApiActivityAdapter;
import com.novamens.kbee.content.webapi.type.ApiContentActivityAdapter;
import com.novamens.kbee.content.webapi.type.IActivityProxyAdapter;
import com.novamens.kbee.content.webapi.type.ICertificateAdapter;
import com.novamens.kbee.content.webapi.type.IFolderAdapter;
import com.novamens.kbee.content.webapi.type.IClassifierAdapter;
import com.novamens.kbee.content.webapi.type.ICommandAdapter;
import com.novamens.kbee.content.webapi.type.IDataSetAdapter;
import com.novamens.kbee.content.webapi.type.IDeviceAdapter;
import com.novamens.kbee.content.webapi.type.IDocAdapter;
import com.novamens.kbee.content.webapi.type.IDomainAdapter;
import com.novamens.kbee.content.webapi.type.IEmailTemplateAdapter;
import com.novamens.kbee.content.webapi.type.IFacetAdapter;
import com.novamens.kbee.content.webapi.type.IFormAdapter;
import com.novamens.kbee.content.webapi.type.IGroupAdapter;
import com.novamens.kbee.content.webapi.type.ILauncherAdapter;
import com.novamens.kbee.content.webapi.type.ILauncherGroupAdapter;
import com.novamens.kbee.content.webapi.type.ILibraryAdapter;
import com.novamens.kbee.content.webapi.type.ILogEventAdapter;
import com.novamens.kbee.content.webapi.type.IProxyAdapter;
import com.novamens.kbee.content.webapi.type.IResourceAdapter;
import com.novamens.kbee.content.webapi.type.IResourceTagAdapter;
import com.novamens.kbee.content.webapi.type.IRoleAdapter;
import com.novamens.kbee.content.webapi.type.ISecurityRuleAdapter;
import com.novamens.kbee.content.webapi.type.ISettingsAdapter;
import com.novamens.kbee.content.webapi.type.ISignatureAdapter;
import com.novamens.kbee.content.webapi.type.ISuggestionAdapter;
import com.novamens.kbee.content.webapi.type.ITemplateAdapter;
import com.novamens.kbee.content.webapi.type.ITokenAdapter;
import com.novamens.kbee.content.webapi.type.IUserAdapter;
import com.novamens.kbee.content.webapi.type.IValueAdapter;
import com.novamens.kbee.content.webapi.type.IPendingTaskAdapter;
import com.novamens.kbee.content.webapi.type.IPendingTaskProxyAdapter;
import com.novamens.kbee.content.webapi.type.IPersonAdapter;
import com.novamens.kbee.content.webapi.type.ListAdapter;
import com.novamens.kbee.content.webapi.type.ResultSetAdapter;
import com.novamens.kbee.content.webapi.type.UriHelper;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.KbeeWorkflowActivity;
import com.novamens.kbee.idoc.webapi.client.RestObjectMapper;
import com.novamens.kbee.portal.service.SiteSearchSuggestionService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.lock.ValueLockerService;
import com.novamens.metrics.SystemMetricsService;
import com.novamens.portal6.model.Site;
import com.novamens.security.AuthToken;
import com.novamens.security.TokenSubmission;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrResultSet;
import com.novamens.workflow.Activity;
import com.novamens.workflow.ActivityProgressNote;
import com.novamens.workflow.Procedure;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/*")
@Api(authorizations = {@Authorization(value = "basicAuth")})
@CrossOrigin(origins="http://localhost:3001")
public class ApiController {

	// ApiLogger must be "Info"
	static private Logger logger = LogManager.getLogger("ApiLogger");
    static private kbee.util.logging.Logger kblogger = kbee.util.logging.Logger.getLogger(ApiController.class.getName());

	static private String WEB = "Web"; // Web Source

	/**
	*
	*
	*/
    @ApiOperation(
            value = "List Classifiers",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found ")})
    @RequestMapping(value = "{domain}/classifiers", method = RequestMethod.GET)
    public ResponseEntity<List<ApiClassifier>> getClassifiers(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domainname) {
        TrafficPass pass = ServiceLocator.getService(TrafficControlService.class).getPass();
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        try {
            su(domainname);
            List<ApiClassifier> classifiers = (new ListAdapter<Classifier, ApiClassifier>(new IClassifierAdapter())).adapt(getApiDao().getClassifiers(getDomain()));
            return new ResponseEntity<List<ApiClassifier>>(classifiers, HttpStatus.OK);
        } finally {
            ServiceLocator.getService(TrafficControlService.class).release(pass);
        }
    }
    
    /**
     * 
     */
    @ApiOperation(
            value = "Find a Classifier",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1230 - Classifier not found")})
    @RequestMapping(value = "{domain}/classifiers/{id}", method = RequestMethod.GET)
    public ResponseEntity<ApiClassifier> getClassifier(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domainname,
            @ApiParam(value = "Classifier id", required = true) @PathVariable Long id) {
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        su(domainname);
        Classifier classifier = getApiDao().findClassifierById(id);
        return new ResponseEntity<ApiClassifier>((new IClassifierAdapter()).adapt(classifier), HttpStatus.OK);
    }
    
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "List Attributes",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found ")})
    @RequestMapping(value = "{domain}/attributes", method = RequestMethod.GET)
    public ResponseEntity<List<IModelAttribute>> getAttributes(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domainname) {
        TrafficPass pass = ServiceLocator.getService(TrafficControlService.class).getPass();
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        try {
            su(domainname);
            List<IModelAttribute> attributes = (new ListAdapter<Attribute, IModelAttribute>(new IModelAttributeAdapter())).adapt(getApiDao().getAttributes(getDomain()));
            return new ResponseEntity<List<IModelAttribute>>(attributes, HttpStatus.OK);
        } 
        finally {
            ServiceLocator.getService(TrafficControlService.class).release(pass);
        }
    }
    
    /**
     *
     *
     */
    @ApiOperation(
            value = "Find a Attribute",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1235 - Attribute not found")})
    @RequestMapping(value = "{domain}/attributes/{id}", method = RequestMethod.GET)
    public ResponseEntity<IModelAttribute> getAttribute(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domainname,
            @ApiParam(value = "Attribute id", required = true) @PathVariable Long id) {
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        su(domainname);
        Attribute attribute = getApiDao().findAttributeById(id);
        return new ResponseEntity<IModelAttribute>((new IModelAttributeAdapter()).adapt(attribute), HttpStatus.OK);
    }


    /**
     *
     *
     */
    @ApiOperation(
            value = "List Datasets",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found ")})
    @RequestMapping(value = "{domain}/datasets", method = RequestMethod.GET)
    public ResponseEntity<List<ApiDataSet>> getDataSets(@ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domainname) {
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        su(domainname);
        List<ApiDataSet> datasets = (new ListAdapter<DataSet, ApiDataSet>(new IDataSetAdapter())).adapt(getApiDao().getDataSets(getDomain()));
        return new ResponseEntity<List<ApiDataSet>>(datasets, HttpStatus.OK);
    }

    /**
     *
     */
    @ApiOperation(
            value = "Find a Dataset",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1220 - Dataset not found")})
    @RequestMapping(value = "{domain}/datasets/{id}", method = RequestMethod.GET)
    public ResponseEntity<ApiDataSet> getDataSet(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domainname,
            @ApiParam(value = "Dataset id or name", required = true) @PathVariable String id
    ) {
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        su(domainname);
        DataSet dataset;
        if (StringUtils.isNumeric(id)) {
            dataset = getApiDao().findDataSetById(Long.valueOf(id));
        } else {
            dataset = getApiDao().findDataSetByName(id);
        }
        return new ResponseEntity<ApiDataSet>((new IDataSetAdapter()).adapt(dataset), HttpStatus.OK);
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "List Resource Tags",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found ")})
    @RequestMapping(value = "{domain}/resourcetags", method = RequestMethod.GET)
    public ResponseEntity<List<IResourceTag>> getResourceTags(@ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domainname) {
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        su(domainname);
        List<IResourceTag> tags = (new ListAdapter<ResourceTag, IResourceTag>(new IResourceTagAdapter())).adapt(getApiDao().getResourceTags(getDomain()));
        return new ResponseEntity<List<IResourceTag>>(tags, HttpStatus.OK);
    }    

    /**
     *
     */
    @ApiOperation(
            value = "Find a Resource Tag",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1230 - Tag not found")})
    @RequestMapping(value = "{domain}/resourcetags/{id}", method = RequestMethod.GET)
    public ResponseEntity<IResourceTag> getResourceTag(
            @ApiParam(value = "Domain Name", required = true) @PathVariable("domain") String domainname,
            @ApiParam(value = "Tag Id", required = true) @PathVariable Long id) {
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        su(domainname);
        ResourceTag tag = getApiDao().findResourceTagById(id);
        return new ResponseEntity<IResourceTag>((new IResourceTagAdapter()).adapt(tag), HttpStatus.OK);
    }
    
    
    /**
     *
     */
    @ApiOperation(
            value = "List Launchers",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found ")})
    @RequestMapping(value = "{domain}/launchers", method = RequestMethod.GET)
    public ResponseEntity<List<ILauncher>> getLaunchers(
    		@ApiParam(value = "Domain Name", required = true) @PathVariable("domain") String domainname,
            @ApiParam(value = "Files Multiplicity", required = false) @RequestParam("files") Optional<String> files) {
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        su(domainname);
        Multiplicity multiplicity = Multiplicity.M0N; 
        if (files.isPresent()) {
        	if ("11".equals(files.get()))
        		multiplicity = Multiplicity.M11;
        	if ("1N".equals(files.get()))
        		multiplicity = Multiplicity.M1N;
        }
        List<ILauncher> launchers = (new ListAdapter<ProcessLauncher, ILauncher>(new ILauncherAdapter())).adapt(getApiDao().getLaunchers(getDomain(), multiplicity));
        return new ResponseEntity<List<ILauncher>>(launchers, HttpStatus.OK);
    }    

    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "List Launchers Groups",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found ")})
    @RequestMapping(value = "{domain}/launchergroups", method = RequestMethod.GET)
    public ResponseEntity<List<ILauncherGroup>> getLauncherGroups(@ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domainname) {
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        su(domainname);
        List<ILauncherGroup> groups = (new ListAdapter<LauncherGroup, ILauncherGroup>(new ILauncherGroupAdapter())).adapt(getApiDao().getLauncherGroups(getDomain()));
        return new ResponseEntity<List<ILauncherGroup>>(groups, HttpStatus.OK);
    }    

    /**
     *
     */
    @ApiOperation(
            value = "Find a Resource Tag",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1230 - Group not found")})
    @RequestMapping(value = "{domain}/launchergroups/{id}", method = RequestMethod.GET)
    public ResponseEntity<ILauncherGroup> getLauncherGroup(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domainname,
            @ApiParam(value = "group id", required = true) @PathVariable Long id) {
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        su(domainname);
        LauncherGroup group = getApiDao().findLauncherGroupById(id);
        return new ResponseEntity<ILauncherGroup>((new ILauncherGroupAdapter()).adapt(group), HttpStatus.OK);
    }
    
    /**
     *
     */
    @ApiOperation(
            value = "List Libraries",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found ")})
    @RequestMapping(value = "{domain}/libraries", method = RequestMethod.GET)
    public ResponseEntity<List<ILibrary>> getLibraries(@ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domainname) {
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        su(domainname);
        List<ILibrary> libraries = (new ListAdapter<Library, ILibrary>(new ILibraryAdapter())).adapt(getApiDao().getLibraries(getDomain()));
        return new ResponseEntity<List<ILibrary>>(libraries, HttpStatus.OK);
    }
    
    /**
     *
     */
    @ApiOperation(
            value = "List Facets",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found ")})
    @RequestMapping(value = "{domain}/facets", method = RequestMethod.GET)
    public ResponseEntity<List<IFacet>> getFacets(@ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domainname) {
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        su(domainname);
        List<IFacet> facets = (new ListAdapter<Facet, IFacet>(new IFacetAdapter())).adapt(getApiDao().getFacets(getDomain()));
        return new ResponseEntity<List<IFacet>>(facets, HttpStatus.OK);
    }    

    /**
     *
     */
    @ApiOperation(
            value = "List Content Templates",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found")})
    @RequestMapping(value = "{domain}/templates", method = RequestMethod.GET)
    public ResponseEntity<List<ITemplate>> getTemplates(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domainname
    ) {
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        su(domainname);
        List<ITemplate> templates = (new ListAdapter<ContentTemplate, ITemplate>(new ITemplateAdapter())).adapt(getApiDao().getTemplates(getDomain()));
        return new ResponseEntity<List<ITemplate>>(templates, HttpStatus.OK);
    }
    
    

    /**
     *
     */
    @ApiOperation(
            value = "Find a Content Template",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1250 - Class not found")})
    @RequestMapping(value = "{domain}/templates/{id}", method = RequestMethod.GET)
    public ResponseEntity<ITemplate> getTemplate(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domainname,
            @ApiParam(value = "Content template id", required = true) @PathVariable Long id) {
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        su(domainname);
        ContentTemplate template = getApiDao().findTemplateById(id);
        return new ResponseEntity<ITemplate>((new ITemplateAdapter()).adapt(template), HttpStatus.OK);
    }
    
    /**
     *
     */
    @ApiOperation(
            value = "Find a EForm",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1245 - Form not found")})
    @RequestMapping(value = "{domain}/forms/{id}", method = RequestMethod.GET)
    public ResponseEntity<IForm> getForm(
            @ApiParam(value = "Form id", required = true) @PathVariable Long id) {
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        EForm form = getApiDao().findFormById(id);
        return new ResponseEntity<IForm>((new IFormAdapter()).adapt(form), HttpStatus.OK);
    }
    
    /**
     *
     */
    @ApiOperation(
            value = "List Procedures",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found")})
    @RequestMapping(value = "{domain}/procedures", method = RequestMethod.GET)
    public ResponseEntity<List<ApiProxy>> getProcedures(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domainname
    ) {
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        su(domainname);
        List<ApiProxy> procedures = (new ListAdapter<Procedure, ApiProxy>(new IProxyAdapter<Procedure>())).adapt(getApiDao().getProcedures(getDomain()));
        return new ResponseEntity<List<ApiProxy>>(procedures, HttpStatus.OK);
    }
    
    /**
     *
     */
    @ApiOperation(
            value = "Find Procedure",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found")})
    @RequestMapping(value = "{domain}/procedures/{id}", method = RequestMethod.GET)
    public ResponseEntity<ApiProcedure> getProcedure(
            @ApiParam(value = "Procedure id", required = true) @PathVariable Long id) {
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        Procedure procedure = getApiDao().findProcedureById(id);
        return new ResponseEntity<ApiProcedure>((new IProcedureAdapter()).adapt(procedure), HttpStatus.OK);
    }
    
    /**
    * 
    */
   @ApiOperation(
           value = "Get Settings",
           authorizations = {@Authorization(value = "basicAuth")})
   @ApiResponses(value = {
           @ApiResponse(code = 200, message = "Ok"),
           @ApiResponse(code = 403, message = "1010 - Access denied"),
           @ApiResponse(code = 404, message = "1200 - Domain not found")})
   @RequestMapping(value = "{domain}/settings", method = RequestMethod.GET)
   public ResponseEntity<ISettings> getSettings(
           @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domainname) {
       ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
       su(domainname);
       return new ResponseEntity<ISettings>((new ISettingsAdapter()).adapt(getDomain()), HttpStatus.OK);
   }
   
   /**
    * 
    * EMAIL TEMPLATES
    */
   @ApiOperation(
           value = "List Email Templates",
           authorizations = {@Authorization(value = "basicAuth")})
   @ApiResponses(value = {
           @ApiResponse(code = 200, message = "Ok"),
           @ApiResponse(code = 403, message = "1010 - Access denied"),
           @ApiResponse(code = 404, message = "1200 - Domain not found")})
   @RequestMapping(value = "{domain}/emailtemplates", method = RequestMethod.GET)
   public ResponseEntity<IResponse<IEmailTemplate>> getEmailTemplates(
           @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domainname,
           @ApiParam(value = "Offset", required = false) @RequestParam("offset") Optional<Long> offset,
           @ApiParam(value = "Page size", required = false) @RequestParam("pageSize") Optional<Integer> pageSize) {
       ResultSet resultSet = null;

       try {
           ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();

           su(domainname);

           resultSet = getApiDao().getEmailTemplates(getDomain());

           if (offset.isPresent() && offset.get().intValue() > 0) {
               resultSet.absolute(offset.get().intValue());
           }

           int pageSizeValue = 25;
           if (pageSize.isPresent()) {
               pageSizeValue = pageSize.get();
           }

           IResponse<IEmailTemplate> iResultSet = (new ResultSetAdapter<EmailTemplate, IEmailTemplate>(new IEmailTemplateAdapter(), pageSizeValue)).adapt(resultSet);

           if (offset.isPresent()) {
               iResultSet.setOffset(offset.get());
           }

           return new ResponseEntity<IResponse<IEmailTemplate>>(iResultSet, HttpStatus.OK);
       } 
       finally {
           if (resultSet != null)
               resultSet.close();
       }
   }
   
   /**
    * 
    */
   @ApiOperation(
           value = "Find Email Template",
           authorizations = {@Authorization(value = "basicAuth")})
   @ApiResponses(value = {
           @ApiResponse(code = 200, message = "Ok"),
           @ApiResponse(code = 403, message = "1010 - Access denied"),
           @ApiResponse(code = 404, message = "1200 - Domain not found")})
   @RequestMapping(value = "{domain}/emailtemplates/{language}/{key}", method = RequestMethod.GET)
   public ResponseEntity<IEmailTemplate> getEmailTemplate(
           @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domainname,
           @ApiParam(value = "Language", required = true) @PathVariable("language") String language,
           @ApiParam(value = "Key", required = true) @PathVariable("key") String key) {
       ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
       su(domainname);
       EmailTemplate template = getApiDao().findEmailTemplate(getDomain(), language, key);
       return new ResponseEntity<IEmailTemplate>((new IEmailTemplateAdapter()).adapt(template), HttpStatus.OK);
   }
   
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     * DATASETS
     */
    @ApiOperation(
            value = "List Dataset values",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1220 - Dataset not found\n1100 - Not Data")})
    @RequestMapping(value = "{domain}/datasets/{dataset}/values", method = RequestMethod.GET)
    public ResponseEntity<IResponse<ApiValue>> getValues(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domainname,
            @ApiParam(value = "Dataset id or name", required = true) @PathVariable("dataset") String datasetId,
            @ApiParam(value = "Iql criteria", required = false) @RequestParam("s") Optional<String> criteria,
            @ApiParam(value = "Facets", required = false) @RequestParam("facets") Optional<Boolean> facets,
            @ApiParam(value = "Members", required = false) @RequestParam("members") Optional<List<String>> members,
            @ApiParam(value = "Sort", required = false) @RequestParam("sort") Optional<String> sort,
            @ApiParam(value = "Offset", required = false) @RequestParam("offset") Optional<Long> offset,
            @ApiParam(value = "Page size", required = false) @RequestParam("pageSize") Optional<Integer> pageSize) {
        ResultSet resultSet = null;

        try {
            ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();

            su(domainname);

            Long datasetNumericId;

            if (StringUtils.isNumeric(datasetId)) {
                datasetNumericId = Long.valueOf(datasetId);
            } else {
                DataSet dataset = getApiDao().findDataSetByName(datasetId);
                datasetNumericId = (long) dataset.getId();
            }

            String criteriavalue = criteria.isPresent() ? criteria.get().toString() : null;
            
            boolean facetsValue = false;
            if (facets.isPresent()) {
            	facetsValue = facets.get(); 
            }
            
            String sortCriteria = null;
            if (sort.isPresent()) {
            	sortCriteria = sort.get();
            }
            
            List<String> membersValue = new ArrayList<String>();
            if (members.isPresent()) {
            	membersValue = members.get(); 
            }

            resultSet = getApiDao().getValues(datasetNumericId, criteriavalue, membersValue, sortCriteria, facetsValue);
        	
            if (offset.isPresent() && offset.get().intValue() > 0) {
                resultSet.absolute(offset.get().intValue());
            }

            int pageSizeValue = 25;
            if (pageSize.isPresent()) {
                pageSizeValue = pageSize.get();
            }
            

            IResponse<ApiValue> iResultSet = (new ResultSetAdapter<DataSetMember, ApiValue>(getDomain(), null, new IValueAdapter(), pageSizeValue, facetsValue)).adapt(resultSet);

            if (offset.isPresent()) {
                iResultSet.setOffset(offset.get());
            }

            return new ResponseEntity<IResponse<ApiValue>>(iResultSet, HttpStatus.OK);
        } finally {
            if (resultSet != null)
                resultSet.close();
        }
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Get dataset value",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1220 - Dataset not found\n1100 - Not found")})
    @RequestMapping(value = "/{domain}/datasets/{dataset}/values/{id}", method = RequestMethod.GET)
    public ResponseEntity<ApiValue> getValue(
            @ApiParam(value = "Domain name", required = true) @PathVariable String domain,
            @ApiParam(value = "Dataset id or name", required = true) @PathVariable("dataset") String datasetId,
            @ApiParam(value = "Dataset value id", required = true) @PathVariable Long id) {
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        su(domain);
        if (!userIs(KbeeGlobalRole.DOMAIN_ADMIN) && !userIs(KbeeGlobalRole.DATASET_VALUES_READ)) {
            throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
        }
        DataSetMember member = getApiDao().findValueById(id);
        if (!member.getDomain().equals(getDomain())) {
            throw new ApiException(HttpStatus.NOT_FOUND, ApiError.USER_NOT_FOUND);
        }
        ApiValue value = (new IValueAdapter()).adapt(member);
        return new ResponseEntity<ApiValue>(value, HttpStatus.OK);
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Get dataset value",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1220 - Dataset not found\n1240 - Value not found")})
    @RequestMapping(value = "/{domain}/datasets/{dataset}/externalvalues/{id}", method = RequestMethod.GET)
    public ResponseEntity<ApiValue> getExternalValue(
            @ApiParam(value = "Domain name", required = true) @PathVariable String domain,
            @ApiParam(value = "Dataset id or name", required = true) @PathVariable("dataset") String datasetId,
            @ApiParam(value = "Dataset external unique id of the value to retrieve", required = true) @PathVariable String id) {
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        su(domain);
        if (!userIs(KbeeGlobalRole.DOMAIN_ADMIN) && !userIs(KbeeGlobalRole.DATASET_VALUES_READ)) {
            throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
        }
        DataSetMember member = getApiDao().findValueByEXternalId(id);
        if (!member.getDomain().equals(getDomain())) {
            throw new ApiException(HttpStatus.NOT_FOUND, ApiError.VALUE_NOT_FOUND);
        }
        ApiValue value = (new IValueAdapter()).adapt(member);
        return new ResponseEntity<ApiValue>(value, HttpStatus.OK);
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Update dataset value",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1220 - Dataset not found\n1240 - Value not found"),
            @ApiResponse(code = 412, message = "1450 - Value already exist\n1400 - Attribute not found. Provided attribute not found in dataset\n1240 - Provided attribute value not found"),
    })
    @RequestMapping(value = "/{domain}/datasets/{dataset}/values/{id}", method = RequestMethod.POST)
    public ResponseEntity<ITransaction> updateValue(
            @ApiParam(value = "Domain name", required = true) @PathVariable String domain,
            @ApiParam(value = "Dataset id or name", required = true) @PathVariable("dataset") String datasetId,
            @ApiParam(value = "Dataset id of the value to update", required = true) @PathVariable("id") String valueId,
            @ApiParam(value = "Dataset value", required = true) @RequestBody ApiValue value) {
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsIn().mark();

        su(domain);

        if (!userIs(KbeeGlobalRole.DOMAIN_ADMIN) && !userIs(KbeeGlobalRole.DATASET_VALUES_WRITE)) {
            throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
        }

        ValueUpdateEvent logevent = new ValueUpdateEvent(value);

        logevent.setSource(WEB);

        try {
            ITransaction transaction;
            if ("newvalue".equals(valueId)) {
                transaction = ((ValueCreateHandler) getHandler("value-create")).create(value);
                logevent.setUri(UriHelper.getUri(value));
            } else {
                transaction = ((ValueUpdateHandler) getHandler("value-update")).update(value);
            }
            logevent.setResponse(transaction);
            logger.info(logevent);
            return new ResponseEntity<ITransaction>(transaction, HttpStatus.OK);
        } 
        catch (ApiException e) {
            logevent.setResponse(new IError(e.getErrorCode(), e.getMessage()));
            logevent.setStatus(e.getHttpStatus());
            logger.info(logevent);
            throw e;
        } 
        finally {
            ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
        }
    }
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
        	value = "Get Root Values",
        	authorizations = {@Authorization(value = "basicAuth"), 
        	@Authorization(value = "bearerAuth")})
    @ApiResponses(value = {
    	@ApiResponse(code = 200, message = "Ok"),
    	@ApiResponse(code = 403, message = "1010 - Access denied"),
        @ApiResponse(code = 404, message = "1200 - Domain not found\n"
            + "1220 - Dataset not found\n"
            + "1240 - Value not found")})
    @RequestMapping(value = "/{domain}/datasets/{dataset}/roots", method = RequestMethod.GET)
    public ResponseEntity<List<INode>> getRootValues(
            @ApiParam(value = "Domain name", required = true) 
            	@PathVariable String domain,
            @ApiParam(value = "Dataset id or name", required = true) 
            	@PathVariable("dataset") String datasetId) {
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        su(domain);
        List<INode> roots = (new ListAdapter<TreeNode, INode>(new INodeAdapter()))
        	.adapt(getApiDao().getRootValues(datasetId));
        return new ResponseEntity<List<INode>>(roots, HttpStatus.OK);
    }
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
        	value = "Get Child Values",
        	authorizations = {@Authorization(value = "basicAuth"), 
        	@Authorization(value = "bearerAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n"
            	+ "1220 - Dataset not found\n"
            	+ "1240 - Value not found")})
    @RequestMapping(
    	    value = "/{domain}/datasets/{dataset}/childs", method = RequestMethod.POST
    	)
    public ResponseEntity<List<INode>> getChildValues(
            @ApiParam(value = "Domain name", required = true) 
            	@PathVariable String domain,
            @ApiParam(value = "Dataset id or name", required = true) 
            	@PathVariable("dataset") String datasetId,
            @ApiParam(value = "TreePath", required = true) 
            	@RequestBody INode node) {
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        su(domain);
        DataSet dataSet = getApiDao().findDataSetByName(datasetId);
        List<INode> childs = (new ListAdapter<TreeNode, INode>(new INodeAdapter()))
            .adapt(getApiDao().getChildValues(dataSet, node.getId()));
        return new ResponseEntity<List<INode>>(childs, HttpStatus.OK);
    }

    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     * PERSON
     */
    @ApiOperation(
            value = "Get Person",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1240 - Value not found")})
    @RequestMapping(value = "/{domain}/persons/{id}", method = RequestMethod.GET)
    public ResponseEntity<IPerson> getPerson(
            @ApiParam(value = "Domain name", required = true) @PathVariable String domain,
            @ApiParam(value = "Person id", required = true) @PathVariable String id) {
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        su(domain);
        if (!userIs(KbeeGlobalRole.DOMAIN_ADMIN) && !userIs(KbeeGlobalRole.DATASET_VALUES_READ)) {
            throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
        }
        Person person = getApiDao().findPersonById("p"+id);
        if (!person.getDomain().equals(getDomain())) {
            throw new ApiException(HttpStatus.NOT_FOUND, ApiError.VALUE_NOT_FOUND);
        }
        IPerson value = (new IPersonAdapter()).adapt(person);
        return new ResponseEntity<IPerson>(value, HttpStatus.OK);
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     * SECURITY USERS
     */
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(value = "Login")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied")})
    @RequestMapping(value = "/auth/login", method = RequestMethod.POST)
    public ResponseEntity<ILoginResponse> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {
    	
    	try {
	    	AuthToken token = ServiceLocator.getService(SecurityService.class).createToken(username, password);
	        
	        IToken itoken = new IToken();
	        itoken.setValue(token.getTokenValue());
	        
	        UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
	        Person person = userProfile.getPerson();
	        ApiUser iuser = (new IUserAdapter()).adapt(person);
	        
	        ILoginResponse response = new ILoginResponse();
	        response.setUser(iuser);
	        response.setToken(itoken);
	        
	        return new ResponseEntity<ILoginResponse>(response, HttpStatus.OK);
    	}
    	catch (Exception e) {
            throw new ApiException(HttpStatus.NOT_FOUND, ApiError.VALUE_NOT_FOUND);
    	}
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Get User full record",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found \n1275 - User not found \n1240 - Value not found")})
    @RequestMapping(value = "{domain}/users/{id}", method = RequestMethod.GET)
    public ResponseEntity<ApiUser> getUser(
            @ApiParam(value = "Domain name", required = true) @PathVariable String domain,
            @ApiParam(value = "User id", required = true) @PathVariable Long id,
            @ApiParam(value = "All", required = false) @RequestParam("all") Optional<String> includeall) {
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        su(domain);
        if (!userIs(KbeeGlobalRole.DOMAIN_ADMIN) && !userIs(KbeeGlobalRole.SECURITY)) {
            throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
        }
        Person person = getApiDao().findPersonById(String.valueOf(id));
        if (!person.getDomain().equals(getDomain())) {
            throw new ApiException(HttpStatus.NOT_FOUND, ApiError.USER_NOT_FOUND);
        }
        ApiUser user = (new IUserAdapter()).adapt(person);
        return new ResponseEntity<ApiUser>(user, HttpStatus.OK);
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Get User full record",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1275 - User not found \n1240 - Value not found")})
    @RequestMapping(value = "/users/{id}", method = RequestMethod.GET)
    public ResponseEntity<ApiUser> getUser(
            @ApiParam(value = "User id", required = true) @PathVariable Long id,
        	@ApiParam(value = "All", required = false) @RequestParam("all") Optional<String> all) {
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        Person person = getApiDao().findPersonById("u" + String.valueOf(id));
        if (!person.getDomain().equals(getDomain())) {
            throw new ApiException(HttpStatus.NOT_FOUND, ApiError.USER_NOT_FOUND);
        }
        ApiUser user = (new IUserAdapter(all.isPresent()&&"true".equals(all.get()))).adapt(person);
        return new ResponseEntity<ApiUser>(user, HttpStatus.OK);
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @RequestMapping(value = "/users/external/{id}", method = RequestMethod.GET)
    public ResponseEntity<ApiUser> getExternalUser(@PathVariable String id) {
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        Person person = getApiDao().findPersonByExternalId(id);
        if (!person.getDomain().equals(getDomain())) {
            throw new ApiException(HttpStatus.NOT_FOUND, ApiError.USER_NOT_FOUND);
        }
        ApiUser user = (new IUserAdapter()).adapt(person);
        return new ResponseEntity<ApiUser>(user, HttpStatus.OK);
    }
    


    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @RequestMapping(value = "/users/{id}/audit", method = RequestMethod.GET)
    public ResponseEntity<List<ILogEvent>> getUserAudit(@PathVariable Long id) {
        User user = getApiDao().findUserById(String.valueOf(id));

        List<ILogEvent> events = (new ListAdapter<LogEvent, ILogEvent>(new ILogEventAdapter())).adapt(getApiDao().getAudit(user));

        return new ResponseEntity<List<ILogEvent>>(events, HttpStatus.OK);
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Update User",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 302, message = "1000 - Not modified"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1275 - User not found\n1240 - Provided attribute value not found"),
            @ApiResponse(code = 412, message = "1480 - User already exist\n1290 - Role not found\n1490 - Invalid name\n1400 -Attribute not found. Provided attribute not found in dataset"),
    })
    @RequestMapping(value = "/{domain}/users/{id}", method = RequestMethod.POST)
    public ResponseEntity<ITransaction> updateUser(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domain,
            @ApiParam(value = "User id of the value to update", required = true) @PathVariable("id") String userId,
            @ApiParam(value = "User value", required = true) @RequestBody ApiUser user) {
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsIn().mark();

        su(domain);

        if (!userIs(KbeeGlobalRole.DOMAIN_ADMIN) && !userIs(KbeeGlobalRole.SECURITY)) {
            throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
        }

        UserUpdateEvent logevent = new UserUpdateEvent(user);

        logevent.setSource(WEB);
        logevent.setDomain(domain);

        String lock = "user" + userId;

        try {
            ServiceLocator.getService(ValueLockerService.class).lock(lock);
            ITransaction transaction;
            if ("newuser".equals(userId)) {
                transaction = ((UserCreateHandler) getHandler("user-create")).create(user);
                logevent.setUri(UriHelper.getUri(user));
            } else {
                transaction = ((UserUpdateHandler) getHandler("user-update")).update(user);
            }
            logevent.setResponse(transaction);
            logger.info(logevent);
            return new ResponseEntity<ITransaction>(transaction, HttpStatus.OK);
        } 
        catch (ApiException e) {
            logevent.setResponse(new IError(e.getErrorCode(), e.getMessage()));
            logevent.setStatus(e.getHttpStatus());
            if (e.isAuditable())
                logger.info(logevent);
            throw e;
        } 
        finally {
            ServiceLocator.getService(ValueLockerService.class).unlock(lock);
            ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
        }
    }


    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Enable User",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1275 - User not found"),
    })
    @RequestMapping(value = "/{domain}/users/{id}/enable", method = RequestMethod.POST)
    public ResponseEntity<ITransaction> enableUser(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domain,
            @ApiParam(value = "User id", required = true) @PathVariable("id") String userId) {

        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsIn().mark();

        su(domain);

        if (!userIs(KbeeGlobalRole.DOMAIN_ADMIN) && !userIs(KbeeGlobalRole.SECURITY)) {
            throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
        }

        UserUpdateEvent logevent = new UserUpdateEvent("/" + domain + "/users/" + userId);

        logevent.setSource(WEB);
        logevent.setDomain(domain);

        String lock = "user" + userId;

        try {
            ServiceLocator.getService(ValueLockerService.class).lock(lock);
            Person person = getApiDao().findPersonById(String.valueOf(userId));
            ApiUser user = getUser(person);
            ITransaction transaction = ((UserUpdateHandler) getHandler("user-update")).enable(user);
            logevent.setResponse(transaction);
            logger.info(logevent);
            return new ResponseEntity<ITransaction>(transaction, HttpStatus.OK);
        } 
        catch (ApiException e) {
            logevent.setResponse(new IError(e.getErrorCode(), e.getMessage()));
            logevent.setStatus(e.getHttpStatus());
            logger.info(logevent);
            throw e;
        } 
        finally {
            ServiceLocator.getService(ValueLockerService.class).unlock(lock);
            ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
        }
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Disable User",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1275 - User not found"),
    })
    @RequestMapping(value = "/{domain}/users/{id}/disable", method = RequestMethod.POST)
    public ResponseEntity<ITransaction> disableUser(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domain,
            @ApiParam(value = "User id", required = true) @PathVariable("id") String userId) {

        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsIn().mark();

        su(domain);

        if (!userIs(KbeeGlobalRole.DOMAIN_ADMIN) && !userIs(KbeeGlobalRole.SECURITY)) {
            throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
        }

        UserUpdateEvent logevent = new UserUpdateEvent("/" + domain + "/users/" + userId);

        logevent.setSource(WEB);
        logevent.setDomain(domain);

        String lock = "user" + userId;

        try {
            ServiceLocator.getService(ValueLockerService.class).lock(lock);
            Person person = getApiDao().findPersonById(String.valueOf(userId));
            ApiUser user = getUser(person);
            ITransaction transaction = ((UserUpdateHandler) getHandler("user-update")).disable(user);
            logevent.setResponse(transaction);
            logger.info(logevent);
            return new ResponseEntity<ITransaction>(transaction, HttpStatus.OK);
        } 
        catch (ApiException e) {
            logevent.setResponse(new IError(e.getErrorCode(), e.getMessage()));
            logevent.setStatus(e.getHttpStatus());
            logger.info(logevent);
            throw e;
        } 
        finally {
            ServiceLocator.getService(ValueLockerService.class).unlock(lock);
            ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
        }
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Delete User",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1275 - User not found"),
    })
    @RequestMapping(value = "/{domain}/users/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<ITransaction> deleteUser(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domain,
            @ApiParam(value = "User id", required = true) @PathVariable("id") Long userId) {

        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsIn().mark();

        su(domain);

        if (!userIs(KbeeGlobalRole.DOMAIN_ADMIN) && !userIs(KbeeGlobalRole.SECURITY)) {
            throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
        }

        UserDeleteEvent logevent = new UserDeleteEvent("/" + domain + "/users/" + userId);

        logevent.setSource(WEB);
        logevent.setDomain(domain);

        String lock = "user" + userId;

        try {
            ServiceLocator.getService(ValueLockerService.class).lock(lock);
            Person person = getApiDao().findPersonById(String.valueOf(userId));
            ApiUser user = getUser(person);
            ITransaction transaction = ((UserDeleteHandler) getHandler("user-delete")).delete(user);
            logevent.setResponse(transaction);
            logger.info(logevent);
            return new ResponseEntity<ITransaction>(transaction, HttpStatus.OK);
        } catch (ApiException e) {
            logevent.setResponse(new IError(e.getErrorCode(), e.getMessage()));
            logevent.setStatus(e.getHttpStatus());
            logger.info(logevent);
            throw e;
        } finally {
            ServiceLocator.getService(ValueLockerService.class).unlock(lock);
            ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
        }
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Delete User",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 302, message = "1000 - Not modified"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1275 - User not found"),
    })
    @RequestMapping(value = "/{domain}/users/external/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<ITransaction> deleteExternalUser(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domain,
            @ApiParam(value = "User external id", required = true) @PathVariable("id") String userId) {

        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsIn().mark();

        su(domain);

        if (!userIs(KbeeGlobalRole.DOMAIN_ADMIN) && !userIs(KbeeGlobalRole.SECURITY)) {
            throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
        }

        UserDeleteEvent logevent = new UserDeleteEvent("/" + domain + "/users/external/" + userId);

        logevent.setSource(WEB);
        logevent.setDomain(domain);

        String lock = "user" + userId;

        try {
            ServiceLocator.getService(ValueLockerService.class).lock(lock);
            Person person = getApiDao().findPersonByExternalId(userId);
            ApiUser user = getUser(person);
            ITransaction transaction = ((UserDeleteHandler) getHandler("user-delete")).delete(user);
            logevent.setResponse(transaction);
            logger.info(logevent);
            return new ResponseEntity<ITransaction>(transaction, HttpStatus.OK);
        } 
        catch (ApiException e) {
            logevent.setResponse(new IError(e.getErrorCode(), e.getMessage()));
            logevent.setStatus(e.getHttpStatus());
            logger.info(logevent);
            throw e;
        } 
        finally {
            ServiceLocator.getService(ValueLockerService.class).unlock(lock);
            ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
        }
    }
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
    	value = "Get User Data (Dashboard)",
    	authorizations = {@Authorization(value = "basicAuth"), 
    	@Authorization(value = "bearerAuth")})
    @ApiResponses(value = {
    	@ApiResponse(code = 200, message = "Ok"),
    	@ApiResponse(code = 403, message = "1010 - Access denied"),
    	@ApiResponse(code = 404, message = "1275 - User not found")})
    @RequestMapping(value = "/{domain}/user/data", method = RequestMethod.GET)
    public ResponseEntity<IUserDashboard> getUserData(
    	@ApiParam(value = "Domain name", required = true) 
            @PathVariable("domain") String domain) {
    	
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        
        IUserDashboard userdata = ((UserDashboardHandler) getHandler("user-dashborad")).get(getUser());

        return new ResponseEntity<IUserDashboard>(userdata, HttpStatus.OK);
    }
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Get User Activities",
            authorizations = {@Authorization(value = "basicAuth"), @Authorization(value = "bearerAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1275 - Device not found"),
    })
    @RequestMapping(value = "/{domain}/user/activities", method = RequestMethod.GET)
    public ResponseEntity<IResponse<IActivityProxy>> getUserActivities(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domain,
            @ApiParam(value = "Offset", required = false) @RequestParam("offset") Optional<Long> offset,
            @ApiParam(value = "Page size", required = false) @RequestParam("pageSize") Optional<Integer> pageSize,
            @ApiParam(value = "Facets", required = false) @RequestParam("facets") Optional<Boolean> facets,
            @ApiParam(value = "Members", required = false) @RequestParam("members") Optional<List<String>> members,
            @ApiParam(value = "Sort", required = false) @RequestParam("sort") Optional<String> sort,
            @RequestHeader(value="Device", required=false) String deviceId) {
    	
    	checkDevice(deviceId);
    	
        TrafficPass pass = ServiceLocator.getService(TrafficControlService.class).getPass();
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        ResultSet resultSet = null;
        try {
        	
            boolean facetsValue = false;
            if (facets.isPresent()) {
            	facetsValue = facets.get(); 
            }
            
            List<String> membersValue = new ArrayList<String>();
            if (members.isPresent()) {
            	membersValue = members.get(); 
            }
            
            String sortCriteria = null;
            if (sort.isPresent()) {
            	sortCriteria = sort.get();
            }
            
            User user = getUser();
            
            resultSet = getApiDao().getUserActivities(user, membersValue, sortCriteria, facetsValue);
            
            if (offset.isPresent()) {
            	int value = offset.get().intValue();
            	if (value==0) value=1;
                resultSet.absolute(value);
            }

            int pageSizeValue = 25;
            if (pageSize.isPresent()) {
                pageSizeValue = pageSize.get();
            }

            IResponse<IActivityProxy> iResultSet = (new ResultSetAdapter<Content, IActivityProxy>(
            		((KbeeUser)user).getDomain(),
            		"workspace",
            		new IActivityProxyAdapter(), 
            		pageSizeValue, 
            		facetsValue)).adapt(resultSet);

            
            return new ResponseEntity<IResponse<IActivityProxy>>(iResultSet, HttpStatus.OK);
        } 
        finally {
            ServiceLocator.getService(TrafficControlService.class).release(pass);
            if (resultSet!=null) resultSet.close();
        }
    }
    
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Get Monitor Activities",
            authorizations = {@Authorization(value = "basicAuth"), 
            		@Authorization(value = "bearerAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1275 - Device not found"),
    })
    @RequestMapping(value = "/{domain}/monitor/proxies", method = RequestMethod.GET)
    public ResponseEntity<IResponse<IActivityProxy>> getMonitorProxies(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domain,
            @ApiParam(value = "Offset", required = false) @RequestParam("offset") Optional<Long> offset,
            @ApiParam(value = "Page size", required = false) @RequestParam("pageSize") Optional<Integer> pageSize,
            @ApiParam(value = "Facets", required = false) @RequestParam("facets") Optional<Boolean> facets,
            @ApiParam(value = "Members", required = false) @RequestParam("members") Optional<List<String>> members,
            @ApiParam(value = "Sort", required = false) @RequestParam("sort") Optional<String> sort,
            @RequestHeader(value="Device", required=false) String deviceId) {
    	
    	checkDevice(deviceId);
    	
        TrafficPass pass = ServiceLocator.getService(TrafficControlService.class).getPass();
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        ResultSet resultSet = null;
        try {
        	
            boolean facetsValue = false;
            if (facets.isPresent()) {
            	facetsValue = facets.get(); 
            }
            
            List<String> membersValue = new ArrayList<String>();
            if (members.isPresent()) {
            	membersValue = members.get(); 
            }
            
            String sortCriteria = null;
            if (sort.isPresent()) {
            	sortCriteria = sort.get();
            }
            
            User user = getUser();
            
            resultSet = getApiDao().getMonitorActivities(membersValue, sortCriteria, facetsValue);
            
            if (offset.isPresent()) {
            	int value = offset.get().intValue();
            	value++;
                resultSet.absolute(value);
            }

            int pageSizeValue = 25;
            if (pageSize.isPresent()) {
                pageSizeValue = pageSize.get();
            }

            IResponse<IActivityProxy> iResultSet = (new ResultSetAdapter<Content, IActivityProxy>(
            		((KbeeUser)user).getDomain(),
            		"monitor",
            		new IActivityProxyAdapter(), 
            		pageSizeValue, 
            		facetsValue)).adapt(resultSet);

            
            return new ResponseEntity<IResponse<IActivityProxy>>(iResultSet, HttpStatus.OK);
        } 
        finally {
            ServiceLocator.getService(TrafficControlService.class).release(pass);
            if (resultSet!=null) resultSet.close();
        }
    }
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
    	value = "Get Monitor Activities",
    	authorizations = {
    		@Authorization(value = "basicAuth"), 
            @Authorization(value = "bearerAuth")})
    @ApiResponses(value = {
    	@ApiResponse(code = 200, message = "Ok"),
    	@ApiResponse(code = 404, message = 
    		"1200 - Domain not found\n" + 
    		"1275 - Device not found"),
    })
    @RequestMapping(value = "/{domain}/monitor/activities", method = RequestMethod.POST)
    public ResponseEntity<IResponse<ApiActivity>> getMonitorActivities(
            @ApiParam(value = "Domain", required = true) 
            	@PathVariable("domain") String domain,
            @RequestBody ApiSearch search,
            @RequestHeader(value="Device", required=false) String deviceId) {
    	
    	checkDevice(deviceId);
    	
        TrafficPass pass = ServiceLocator.getService(TrafficControlService.class).getPass();
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        ResultSet resultSet = null;
        try {
        	
            boolean facetsValue = false;
//            if (facets.isPresent()) {
//            	facetsValue = facets.get(); 
//            }
//            
            List<String> membersValue = new ArrayList<String>();
//            if (members.isPresent()) {
//            	membersValue = members.get(); 
//            }
//            
            String sortCriteria = null;
//            if (sort.isPresent()) {
//            	sortCriteria = sort.get();
//            }
//            
            User user = getUser();
//            
//            
//            resultSet = getApiDao().getFiles(
//            		library, 
//            		search.getFilters(), 
//            		search.getSort(), 
//            		search.getFacets());
//            
            
            
            resultSet = getApiDao().getMonitorActivities(membersValue, sortCriteria, facetsValue);
            
//            if (offset.isPresent()) {
//            	int value = offset.get().intValue();
//            	value++;
//                resultSet.absolute(value);
//            }

            int pageSizeValue = 25;
//            if (pageSize.isPresent()) {
//                pageSizeValue = pageSize.get();
//            }

            IResponse<ApiActivity> iResultSet = (new ResultSetAdapter<Content, ApiActivity>(
            		((KbeeUser)user).getDomain(),
            		"monitor",
            		new ApiContentActivityAdapter(), 
            		pageSizeValue, 
            		facetsValue)).adapt(resultSet);

            
            return new ResponseEntity<IResponse<ApiActivity>>(iResultSet, HttpStatus.OK);
        } 
        finally {
            ServiceLocator.getService(TrafficControlService.class).release(pass);
            if (resultSet!=null) resultSet.close();
        }
    }
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Get WorkItems",
            authorizations = {@Authorization(value = "basicAuth"), @Authorization(value = "bearerAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1275 - Device not found"),
    })
    @RequestMapping(value = "/{domain}/pendingtasks", method = RequestMethod.GET)
    public ResponseEntity<IResponse<IPendingTaskProxy>> getWorkItems(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domain,
            @ApiParam(value = "Offset", required = false) @RequestParam("offset") Optional<Long> offset,
            @ApiParam(value = "Page size", required = false) @RequestParam("pageSize") Optional<Integer> pageSize,
            @ApiParam(value = "Facets", required = false) @RequestParam("facets") Optional<Boolean> facets,
            @ApiParam(value = "Members", required = false) @RequestParam("members") Optional<List<String>> members,
            @ApiParam(value = "Sort", required = false) @RequestParam("sort") Optional<String> sort,
            @RequestHeader(value="Device", required=false) String deviceId) {
    	
    	checkDevice(deviceId);
    	
        TrafficPass pass = ServiceLocator.getService(TrafficControlService.class).getPass();
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        ResultSet resultSet = null;
        try {
        	
            boolean facetsValue = false;
            if (facets.isPresent()) {
            	facetsValue = facets.get(); 
            }
            
            List<String> membersValue = new ArrayList<String>();
            if (members.isPresent()) {
            	membersValue = members.get(); 
            }
            
            String sortCriteria = null;
            if (sort.isPresent()) {
            	sortCriteria = sort.get();
            }
            
            User user = getUser();
            
            resultSet = getApiDao().getWorkItems(membersValue, sortCriteria, facetsValue);
            
            if (offset.isPresent()) {
            	int value = offset.get().intValue();
            	if (value==0) value=1;
                resultSet.absolute(value);
            }

            int pageSizeValue = 25;
            if (pageSize.isPresent()) {
                pageSizeValue = pageSize.get();
            }

            IResponse<IPendingTaskProxy> iResultSet = (new ResultSetAdapter<Content, IPendingTaskProxy>(
            		((KbeeUser)user).getDomain(),
            		"pendings",
            		new IPendingTaskProxyAdapter(), 
            		pageSizeValue, 
            		facetsValue)).adapt(resultSet);

            
            return new ResponseEntity<IResponse<IPendingTaskProxy>>(iResultSet, HttpStatus.OK);
        } 
        finally {
            ServiceLocator.getService(TrafficControlService.class).release(pass);
            if (resultSet!=null) resultSet.close();
        }
    }
    
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Get Files",
            authorizations = {@Authorization(value = "basicAuth"), @Authorization(value = "bearerAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1275 - Device not found"),
    })
    @RequestMapping(value = "/{domain}/files", method = RequestMethod.GET)
    public ResponseEntity<IResponse<ApiFile>> getFiles(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domain,
            @ApiParam(value = "Offset", required = false) @RequestParam("offset") Optional<Long> offset,
            @ApiParam(value = "Page size", required = false) @RequestParam("pageSize") Optional<Integer> pageSize,
            @ApiParam(value = "Facets", required = false) @RequestParam("facets") Optional<Boolean> facets,
            @ApiParam(value = "Members", required = false) @RequestParam("members") Optional<List<String>> members,
            @ApiParam(value = "Sort", required = false) @RequestParam("sort") Optional<String> sort,
            @ApiParam(value = "Library", required = false) @RequestParam("library") Optional<String> libraryId,
            @RequestHeader(value="Device", required=false) String deviceId) {
    	
    	checkDevice(deviceId);
    	
        TrafficPass pass = ServiceLocator.getService(TrafficControlService.class).getPass();
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        ResultSet resultSet = null;
        try {
        	
            boolean facetsValue = false;
            if (facets.isPresent()) {
            	facetsValue = facets.get(); 
            }
            
            List<String> membersValue = new ArrayList<String>();
            if (members.isPresent()) {
            	membersValue = members.get(); 
            }
            
            String sortCriteria = null;
            if (sort.isPresent()) {
            	sortCriteria = sort.get();
            }
            
            Library library = null;
            if (libraryId.isPresent()) {
            	library = getApiDao().findLibraryById(Long.valueOf(libraryId.get()));
            }
            	
            User user = getUser();
            
            resultSet = getApiDao().getFiles(library, membersValue, sortCriteria, facetsValue);
            
            if (offset.isPresent()) {
            	int value = offset.get().intValue();
            	if (value==0) value=1;
                resultSet.absolute(value);
            }

            int pageSizeValue = 25;
            if (pageSize.isPresent()) {
                pageSizeValue = pageSize.get();
            }

            IResponse<ApiFile> iResultSet = (new ResultSetAdapter<KbeeIDoc, ApiFile>(
            		((KbeeUser)user).getDomain(),
            		library!=null ? library.getKey() : "all",
            		new IDocAdapter(false), 
            		pageSizeValue, 
            		facetsValue)).adapt(resultSet);

            
            return new ResponseEntity<IResponse<ApiFile>>(iResultSet, HttpStatus.OK);
        } 
        finally {
            ServiceLocator.getService(TrafficControlService.class).release(pass);
            if (resultSet!=null) resultSet.close();
        }
    }
   
    
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
    	value = "Search Files",
    	authorizations = {@Authorization(value = "basicAuth"), 
            @Authorization(value = "bearerAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n"
            	+ "1275 - Device not found"),
    })
    @RequestMapping(value = "/{domain}/files", method = RequestMethod.POST)
    public ResponseEntity<IResponse<ApiFile>> searchFiles(
        @PathVariable("domain") String domain,
        @RequestBody ApiSearch search,
        @RequestHeader(value="Device", required=false) String deviceId) { 
    	
    	checkDevice(deviceId);
    	
        TrafficPass pass = ServiceLocator.getService(TrafficControlService.class).getPass();
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        ResultSet resultSet = null;
        
        try {
        	
            Library library = null;
            if (search.getSite()!=null) {
              	library = getApiDao().findLibraryById(Long.valueOf(search.getSite()));
            }
            
            resultSet = getApiDao().getFiles(
            		library, 
            		search.getFilters(), 
            		search.getSort(), 
            		search.getFacets());
            
           	int offset = search.getPage()*search.getPageSize();
           	if (offset==0) offset=1;
            resultSet.absolute(offset);
 
            IResponse<ApiFile> iResultSet = (new ResultSetAdapter<KbeeIDoc, ApiFile>(
            		((KbeeUser)getUser()).getDomain(),
            		library!=null ? library.getKey() : "all",
            		new IDocAdapter(false), 
            		search.getPageSize(), 
            		search.getFacets())).adapt(resultSet);
            
            return new ResponseEntity<IResponse<ApiFile>>(iResultSet, HttpStatus.OK);
        } 
        finally {
            ServiceLocator.getService(TrafficControlService.class).release(pass);
            if (resultSet!=null) resultSet.close();
        }
    } 
    
    @ApiOperation(
    		value = "Get Suggestions",
    		authorizations = {@Authorization(value = "basicAuth"), @Authorization(value = "bearerAuth")})
    @RequestMapping(value = "/{domain}/{site}/suggestions", method = RequestMethod.GET)
    public ResponseEntity<List<ISuggestion>> getSuggestions(
        @PathVariable("domain") String domain,
        @PathVariable("site") String sitename,
        @RequestParam("text") String text,
        @RequestHeader(value="Device", required=false) String deviceId) { 
    	
    	checkDevice(deviceId);
    	
        TrafficPass pass = ServiceLocator.getService(TrafficControlService.class).getPass();
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        ResultSet resultSet = null;
        
        try {
        	
        	Site site = getApiDao().findSiteByName(sitename);
        	
        	List<Suggestion> suggestions = site.getService(SiteSearchSuggestionService.class).getSuggestions(text);
 
            List<ISuggestion> proxyes = (new ListAdapter<Suggestion, ISuggestion>(new ISuggestionAdapter())).adapt(suggestions);
            
            return new ResponseEntity<List<ISuggestion>>(proxyes, HttpStatus.OK);
        } 
        finally {
            ServiceLocator.getService(TrafficControlService.class).release(pass);
            if (resultSet!=null) resultSet.close();
        }
    }
    
   @ApiOperation(
           value = "Get Folder",
           authorizations = {@Authorization(value = "basicAuth"), @Authorization(value = "bearerAuth")})
   @ApiResponses(value = {
           @ApiResponse(code = 200, message = "Ok"),
           @ApiResponse(code = 404, message = "1200 - Domain not found\n"
           		+ "1275 - Device not found"),
   })
   @RequestMapping(value = "/{domain}/{site}/folder/{dataset}", method = RequestMethod.POST)
   public ResponseEntity<IResponse<ApiObject>> getFolder(
		   @PathVariable("domain") String domain,
		   @PathVariable("site") String sitename,
		   @PathVariable("dataset") String datasetname,
		   @RequestBody ApiSearch search,
		   @RequestHeader(value="Device", required=false) String deviceId) { 
   	
	   checkDevice(deviceId);
	   
	  // checkDomain(domain);
	   TrafficPass pass = ServiceLocator.getService(TrafficControlService.class).getPass();
       ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
       ResultSet resultSet = null;
       
       try {
    	   Site site = getApiDao().findSiteByName(sitename);
    	   DataSet dataset = getApiDao().findDataSetByName(datasetname);
    	   
    	   String nodeid = search.getNode().getId();
    	   String folderid = nodeid.substring(nodeid.lastIndexOf("/")+1);
    	   DataSetMember folder = getApiDao().findValueById(Long.valueOf(folderid)); 	   
           
    	   resultSet = getApiDao().getFolder(
    			site, 
          		dataset, 
          		folder, 
          		search.getSort());
           
          	int offset = search.getPage()*search.getPageSize();
          	if (offset==0) offset=1;
          	resultSet.absolute(offset);

            IResponse<ApiObject> iResultSet = (new ResultSetAdapter<Classificable, ApiObject>(
            		getDomain(),
            		"all",
            		new IFolderAdapter(search.getNode()), 
            		search.getPageSize(), 
            		search.getFacets())).adapt(resultSet);
           
           return new ResponseEntity<IResponse<ApiObject>>(iResultSet, HttpStatus.OK);
       } 
       finally {
           ServiceLocator.getService(TrafficControlService.class).release(pass);
           if (resultSet!=null) resultSet.close();
       }
   } 
   
   
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
    		value = "Get Activity",
    		authorizations = {@Authorization(value = "basicAuth"), @Authorization(value = "bearerAuth")})
    @RequestMapping(value = "/{domain}/user/activities/{id}", method = RequestMethod.GET)
    public ResponseEntity<ApiActivity> getActivity(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domain,
    		@PathVariable String id) {
    	
    	try {
    	
	    	String PROPERTY_UNREAD = "unread";
	        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
	        
	        KbeeWorkflowActivity activity = (KbeeWorkflowActivity)getApiDao().findActivityById(Long.valueOf(id));
	        
	        if (activity==null || !activity.getContent().getDomain().equals(getDomain())) {
	            throw new ApiException(HttpStatus.NOT_FOUND, ApiError.ACTIVITY_NOT_FOUND);
	        }
	        
	        if (!activity.getUser().equals(getUser()) && !isReadable(activity.getContent())) {
	            throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
	        }
	        
	        if (activity.getUser().equals(getUser())) {
	        	Content content = activity.getContent();
	    		String nr = (String) content.getService(PropertyService.class).getProperty(PROPERTY_UNREAD);
	    		boolean isUnread = nr!=null && nr.equals("yes");
	    		if (isUnread) {
	    			content.getService(PropertyService.class).updateProperty(PROPERTY_UNREAD, "no");
	    		}
	        }
	        
	        ApiActivity iactivity = (new ApiActivityAdapter()).adapt(activity);
	        
	        return new ResponseEntity<ApiActivity>(iactivity,HttpStatus.OK);
	    }
	    catch (ApiException e) {
	        throw e;
	    } 
	    catch (Exception e) {
	    	logger.error(e);
	    	throw e;
	    } 
	    finally {
	    	ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
	    }
    }
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
    		value = "Get Activity Notes",
    		authorizations = {@Authorization(value = "basicAuth"), @Authorization(value = "bearerAuth")})
    
    @RequestMapping(value = "/{domain}/user/activities/{id}/notes", method = RequestMethod.GET)
    public ResponseEntity<List<INote>> getActivityNotes(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domain,
    		@PathVariable String id) {
    	
    	try {
    	
	        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
	        
	        KbeeWorkflowActivity activity = (KbeeWorkflowActivity)getApiDao().findActivityById(Long.valueOf(id));
	        
	        if (activity==null || !activity.getContent().getDomain().equals(getDomain())) {
	            throw new ApiException(HttpStatus.NOT_FOUND, ApiError.ACTIVITY_NOT_FOUND);
	        }
	        
            List<INote> notes = (new ListAdapter<ActivityProgressNote, INote>(new IProgressNoteAdapter())).adapt(activity.getProgressNotes());
            
    		Collections.sort(notes, new Comparator<INote>() {
    			@Override
    			public int compare(INote a, INote b) {
    				return a.getTime().isAfter(b.getTime()) ? -1 : 1;
    			}
    		}); 

            return new ResponseEntity<List<INote>>(notes, HttpStatus.OK);
	    }
	    catch (ApiException e) {
	        throw e;
	    } 
	    catch (Exception e) {
	    	logger.error(e);
	    	throw e;
	    } 
	    finally {
	    	ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
	    }
    }

    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Get Pending Task",
            authorizations = {@Authorization(value = "basicAuth"), @Authorization(value = "bearerAuth")})
    @RequestMapping(value = "/{domain}/pendingtasks/{id}", method = RequestMethod.GET)
    public ResponseEntity<IPendingTask> getWorkItem(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domain,
    		@PathVariable String id) {
    	
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        
        try {
        
	        Content content = getApiDao().findContentById(Long.valueOf(id));
	        
			if (content==null) {
				throw new ApiException(HttpStatus.NOT_FOUND, ApiError.FILE_NOT_FOUND);
			}
	        
	        WorkflowService ws = content.getService(WorkflowService.class);
	        
			if (ws==null || ws.getContext().getTime()!=null) {
				throw new ApiException(HttpStatus.NOT_FOUND, ApiError.ACTIVITY_NOT_FOUND);
			}
			
	        if (!isReadable(content)) {
	            throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
	        }
	        
	        IPendingTask workItem = (new IPendingTaskAdapter()).adapt(content);
	        
	        return new ResponseEntity<IPendingTask>(workItem, HttpStatus.OK);
        }
        catch (ApiException e) {
            throw e;
        } 
        catch (Exception e) {
        	logger.error(e);
        	throw e;
        } 
        finally {
        	ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
        }
    }
    
    
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Take Task",
            authorizations = {@Authorization(value = "basicAuth"), @Authorization(value = "bearerAuth")})
    @RequestMapping(value = "/{domain}/pendingtasks/{id}/take", method = RequestMethod.POST)
    public ResponseEntity<ITransaction> takeTask(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domain,
            @PathVariable String id,
    		@ApiParam(value = "Device", required = true) @RequestBody IDevice idevice) {
    	
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        
        try {
        	checkOrAddDevice(idevice);
        	ITransaction transaction = ((TakeTaskHandler) getHandler("take-task")).handle(id);
            return new ResponseEntity<ITransaction>(transaction, HttpStatus.OK);
        }
        catch (ApiException e) {
            throw e;
        } 
        catch (Exception e) {
        	logger.error(e);
        	throw e;
        } 
        finally {
        	ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
        }
    }
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @RequestMapping(value = "/{domain}/user/activities/{id}/{event}/collaborators", method = RequestMethod.GET)
    public ResponseEntity<List<ApiUser>> getCollaborators(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domain,
    		@PathVariable String id, @PathVariable String event) {
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        KbeeWorkflowActivity activity = (KbeeWorkflowActivity)getApiDao().findActivityById(Long.valueOf(id));
        if (activity==null || !activity.getContent().getDomain().equals(getDomain())) {
            throw new ApiException(HttpStatus.NOT_FOUND, ApiError.ACTIVITY_NOT_FOUND);
        }
        List<ApiUser> users = (new ListAdapter<Person, ApiUser>(new IUserAdapter())).adapt(getApiDao().getCollaborators(activity, event));
        return new ResponseEntity<List<ApiUser>>(users, HttpStatus.OK);
    }
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @RequestMapping(value = "/{domain}/user/activities/{id}", method = RequestMethod.POST)
    public ResponseEntity<ITransaction> handleActivityEvent(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domain,
    		@PathVariable String id,
    		@ApiParam(value = "Event", required = true) @RequestBody IWorkflowEvent event) {
    	

        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsIn().mark();

        WorkflowEvent logevent = new WorkflowEvent(event);

        logevent.setSource(WEB);

        try {
        	checkOrAddDevice(event.getDevice());
            ITransaction transaction = ((WorkflowEventHandler) getHandler("workflow-event")).handle(event);
            logevent.setResponse(transaction);
            logger.info(logevent);
            return new ResponseEntity<ITransaction>(transaction, HttpStatus.OK);
        } 
        catch (ApiException e) {
            logevent.setResponse(new IError(e.getErrorCode(), e.getMessage()));
            logevent.setStatus(e.getHttpStatus());
            logger.info(logevent);
            throw e;
        } 
        catch (Exception e) {
            logger.error(e);
            throw e;
        } 
        finally {
            ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
        }
    }
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
    		value = "Add Activity Note",
    		authorizations = {@Authorization(value = "basicAuth"), @Authorization(value = "bearerAuth")})
    @RequestMapping(value = "/{domain}/user/activities/{id}/addnote", method = RequestMethod.POST)
    public ResponseEntity<ITransaction> addActivityNote(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domain,
    		@PathVariable String id,
    		@ApiParam(value = "Note", required = true) @RequestBody INote note) {
    	

        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsIn().mark();


        try {
        	//checkOrAddDevice(event.getDevice());
            ITransaction transaction = ((WorkflowActivityHandler) getHandler("workflow-activity")).add(id, note);
            return new ResponseEntity<ITransaction>(transaction, HttpStatus.OK);
        } 
        catch (ApiException e) {
            throw e;
        } 
        catch (Exception e) {
            logger.error(e);
            throw e;
        } 
        finally {
            ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
        }
    }
    
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
    		value = "Update Activity Note",
    		authorizations = {@Authorization(value = "basicAuth"), @Authorization(value = "bearerAuth")})
    @RequestMapping(value = "/{domain}/user/activities/{id}/notes", method = RequestMethod.POST)
    public ResponseEntity<ITransaction> updateActivityNote(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domain,
    		@PathVariable String id,
    		@ApiParam(value = "Note", required = true) @RequestBody INote note) {
    	

        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsIn().mark();


        try {
            ITransaction transaction = ((WorkflowActivityHandler) getHandler("workflow-activity")).update(id, note);
            return new ResponseEntity<ITransaction>(transaction, HttpStatus.OK);
        } 
        catch (ApiException e) {
            throw e;
        } 
        catch (Exception e) {
            logger.error(e);
            throw e;
        } 
        finally {
            ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
        }
    }
    
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
    		value = "Delete Activity Note",
    		authorizations = {@Authorization(value = "basicAuth"), @Authorization(value = "bearerAuth")})
    @RequestMapping(value = "/{domain}/user/activities/{id}/deletenote", method = RequestMethod.DELETE)
    public ResponseEntity<ITransaction> deleteActivityNote(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domain,
    		@PathVariable String id,
    		@ApiParam(value = "Note", required = true) @RequestBody INote note) {
    	

        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsIn().mark();

        try {
            ITransaction transaction = ((WorkflowActivityHandler) getHandler("workflow-activity")).delete(id, note);
            return new ResponseEntity<ITransaction>(transaction, HttpStatus.OK);
        } 
        catch (ApiException e) {
            throw e;
        } 
        catch (Exception e) {
            logger.error(e);
            throw e;
        } 
        finally {
            ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
        }
    }

    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    
    @RequestMapping(value = "/{domain}/user/activities/{id}/cancel", method = RequestMethod.POST)
    public ResponseEntity<ITransaction> cancelActivity(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domain,
    		@PathVariable String id) {
    	
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsIn().mark();

        try {
            ITransaction transaction = ((WorkflowActivityHandler) getHandler("workflow-activity")).cancel(id);
            return new ResponseEntity<ITransaction>(transaction, HttpStatus.OK);
        } 
        catch (ApiException e) {
            logger.error(e);
            throw e;
        } 
        catch (Exception e) {
            logger.error(e);
            throw e;
        } 
        finally {
            ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
        }
    }

    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Sign",
            authorizations = {@Authorization(value = "basicAuth"), @Authorization(value = "bearerAuth")})
    @RequestMapping(value = "/{domain}/user/activities/{activityId}/{eform}/sign", method = RequestMethod.POST)
    public ResponseEntity<ITransaction> sign(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domain,
    		@PathVariable String activityId, 
    		@PathVariable String eform,
    		@ApiParam(value = "Device", required = true) @RequestBody IDevice idevice,
    	    HttpServletRequest request)  {
    	
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        
        try {
	        checkOrAddDevice(idevice);
	    	
//	        KbeeWorkflowActivity activity = (KbeeWorkflowActivity)getApiDao().findActivityById(Long.valueOf(activityId));
//	        if (activity==null || !activity.getContent().getDomain().equals(getDomain())) {
//	            throw new ApiException(HttpStatus.NOT_FOUND, ApiError.ACTIVITY_NOT_FOUND);
//	        }
//	        
//	        if (!activity.getUser().equals(getUser())) {
//	            throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
//	        }
//	        
//	        EForm form = getApiDao().findFormById(Long.valueOf(eform));
//	        if (form==null) {
//	            throw new ApiException(HttpStatus.NOT_FOUND, ApiError.FORM_NOT_FOUND);
//	        }
//	        
//	        EFormData data = activity.getContent().getFormData(new KbeeTaskForm(form));
//	        if (data==null) {
//	            throw new ApiException(HttpStatus.NOT_FOUND, ApiError.NO_DATA);
//	        }
	        
	        ITransaction transaction = ((SignatureHandler) getHandler("signature")).sign(activityId, eform, idevice.getId());
	        
	        return new ResponseEntity<ITransaction>(transaction, HttpStatus.OK);
        } 
        catch (Exception e) {
            logger.error(e);
            throw e;
        } 
        finally {
            ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
        }
    }
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Sign",
            authorizations = {@Authorization(value = "basicAuth"), @Authorization(value = "bearerAuth")})
    @RequestMapping(value = "/{domain}/user/activities/{activityId}/{eform}/{resourceId}/signed", method = RequestMethod.POST)
    public ResponseEntity<ITransaction> getSigned(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domain,
    		@PathVariable String activityId, 
    		@PathVariable String eform,
    		@PathVariable String resourceId,
    		@ApiParam(value = "Device", required = true) @RequestBody IDevice idevice,
    	    HttpServletRequest request)  {
    	
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        
        try {
	        checkOrAddDevice(idevice);
        
	        ITransaction transaction = ((SignatureHandler) getHandler("signature")).getSigned(activityId, eform, resourceId, idevice.getId());
	        
	        return new ResponseEntity<ITransaction>(transaction, HttpStatus.OK);
        } 
        catch (Exception e) {
            logger.error(e);
            throw e;
        } 
        finally {
            ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
        }
    }
        
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Update Signature",
            authorizations = {@Authorization(value = "basicAuth"), @Authorization(value = "bearerAuth")})
    @RequestMapping(value = "/{domain}/user/activities/{activityId}/{eform}/updateSignature", method = RequestMethod.POST)
    public ResponseEntity<ITransaction> updateSignature(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domain,
    		@PathVariable String activityId, 
    		@PathVariable String eform,
            @ApiParam(value = "SignedResource", required = false) @RequestParam("signedResource") Optional<String> signedResource,
    		@ApiParam(value = "SignedData", required = true) @RequestBody ISignedData signedData,
    		@RequestHeader(value="Device", required=false) String deviceId,
    	    HttpServletRequest request)  {
    	
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        
        try {
	        checkDevice(deviceId);
	        String signedResourceId = signedResource.isPresent() ? signedResource.get() : null;
	        ITransaction transaction = ((SignatureHandler) getHandler("signature")).update(activityId, eform, signedResourceId, deviceId, signedData);
	        return new ResponseEntity<ITransaction>(transaction, HttpStatus.OK);
	    } 
	    catch (Exception e) {
	        logger.error(e);
	        throw e;
	    } 
	    finally {
	        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
	    }
    }
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Get Token",
            authorizations = {@Authorization(value = "basicAuth"), @Authorization(value = "bearerAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied\n2600 - Device not registered")
    })
    @RequestMapping(value = "/{domain}/security/token", method = RequestMethod.GET)
    public ResponseEntity<IToken> getToken(
            @ApiParam(value = "Domain Name", required = true) @PathVariable("domain") String domain,
            @RequestHeader(value="Device", required=false) String deviceId) {

        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsIn().mark();
        
    	UserDevice device = getDevice(deviceId);
    	if (device == null) {
    		if (getDomain().getSecurityLevel()>1) {
    			throw new ApiException(HttpStatus.FORBIDDEN, ApiError.DEVICE_NOT_REGISTERED);
    		}
    	}
    	else {
    		if (!ObjectState.ENABLED.equals(device.getState())) {
    			throw new ApiException(HttpStatus.FORBIDDEN, ApiError.DEVICE_NOT_REGISTERED);
    		}
    	}
  
        try {
            AuthToken token = ServiceLocator.getService(UserService.class).getAuthToken();
            IToken itoken = (new ITokenAdapter()).adapt(token);
            return new ResponseEntity<IToken>(itoken, HttpStatus.OK);
        } 
        catch (ApiException e) {
            throw e;
        } 
        finally {
            ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
        }
    }
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Resend Token",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied\n2600 - Device not registered"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1275 - Activity not found"),
    })
    @RequestMapping(value = "/{domain}/user/activities/{id}/resendtoken", method = RequestMethod.POST)
    public ResponseEntity<ApiActivity> resendToken(
            @ApiParam(value = "Domain Name", required = true) @PathVariable("domain") String domain,
            @ApiParam(value = "Activity Id", required = true) @PathVariable String id, 
            @RequestHeader(value="Device", required=false) String deviceId) {

        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsIn().mark();
        
//        if (!registerdDevice(deviceId)) {
//            throw new ApiException(HttpStatus.FORBIDDEN, ApiError.DEVICE_NOT_REGISTERED);
//        }
        
        KbeeWorkflowActivity activity = (KbeeWorkflowActivity)getApiDao().findActivityById(Long.valueOf(id));

        if (activity==null || !activity.getContent().getDomain().equals(getDomain())) {
            throw new ApiException(HttpStatus.NOT_FOUND, ApiError.ACTIVITY_NOT_FOUND);
        }
        
        if (!activity.getUser().equals(getUser())) {
            throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
        }
        
        Content content = activity.getContent();
        KbeeContext context = (KbeeContext)content.getService(WorkflowService.class).getContext();
        
        Person person = getApiDao().findPersonById(context.getParameter("delivery-person"));
        
        if (person==null) {
            throw new ApiException(HttpStatus.NOT_FOUND, ApiError.USER_NOT_FOUND);
        }
        
        TokenSubmission submission = person.getService(PersonService.class).sendToken(content);

        Map<String, String> parameters = context.getParameters();
        parameters.put("delivery-token", submission.getTokenValue());
        parameters.put("delivery-email", submission.getEmail());
        parameters.put("delivery-phone", submission.getPhone());
        parameters.put("delivery-error", String.valueOf(submission.hasError()));
        parameters.put("delivery-feedback", submission.getFeedback());
        
        content.getService(WorkflowService.class).setParameters(parameters);
        
        ApiActivity iactivity = (new ApiActivityAdapter()).adapt(activity);
        
        return new ResponseEntity<ApiActivity>(iactivity, HttpStatus.OK);
    }
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Add Device",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found"),
    })
    	
    @RequestMapping(value = "/{domain}/security/device", method = RequestMethod.POST)
    public ResponseEntity<ITransaction> addDevice(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domain, 
        	@ApiParam(value = "Device value", required = true) @RequestBody IDevice device) {

        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsIn().mark();

        su(domain);
  
        try {
            ITransaction transaction = ((DeviceUpdateHandler) getHandler("device-add")).add(device);
            return new ResponseEntity<ITransaction>(transaction, HttpStatus.OK);
        } 
        catch (ApiException e) {
            throw e;
        } 
        finally {
            ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
        }
    }
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Update Signature",
            authorizations = {@Authorization(value = "basicAuth"), @Authorization(value = "bearerAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied")
    })
    @RequestMapping(value = "/{domain}/security/signature", method = RequestMethod.POST)
    public ResponseEntity<ITransaction> updateSignature(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domain, 
     		@ApiParam(value = "Signature value", required = true) @RequestBody ISignature signature) {
     
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsIn().mark();
   
        try {
            checkOrAddDevice(signature.getDevice());
            ITransaction transaction = ((SignatureUpdateHandler) getHandler("signature-update")).update(signature);
            return new ResponseEntity<ITransaction>(transaction, HttpStatus.OK);
        } 
        catch (ApiException e) {
            throw e;
        }
        catch (Exception e) {
        	kblogger.error(e);
            throw e;
        } 
        finally {
            ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
        }
    }
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Get Signatures",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found"),
    })
    	
    @RequestMapping(value = "/{domain}/security/signatures", method = RequestMethod.GET)
    public ResponseEntity<List<ISignature>> getSignatures(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domain, 
         	@RequestHeader(value="Device", required=false) String deviceId) {
    	
    	try {
	        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsIn().mark();
	
	        checkDevice(deviceId);
	
	        List<UserSignature> allsignatures = getApiDao().getSignatures();
	        List<UserSignature> signatures = new ArrayList<>();
	        
	        for (UserSignature signature : allsignatures) {
	        	if (deviceId!=null && (signature.getDevice()!=null && deviceId.equals(signature.getDevice().getDeviceId()))) {
	        		signatures.add(signature);
	        	}
	        }
	        
	        List<ISignature> isignatures = (new ListAdapter<UserSignature, ISignature>(new ISignatureAdapter())).adapt(signatures);
	
	        return new ResponseEntity<List<ISignature>>(isignatures, HttpStatus.OK);
    	}
    	catch (Exception e) {
    		kblogger.error(e);
    		throw e;
    	}
    }

    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Get Session User",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 404, message = "1200 - Domain not found")})
    @RequestMapping(value = "{domain}/security/user", method = RequestMethod.GET)
    public ResponseEntity<ApiUser> getSessionUser(
            @ApiParam(value = "Domain name", required = true) @PathVariable String domain) {
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        Person person = getApiDao().findPersonById("u"+String.valueOf(getUser().getId()));
        ApiUser user = (new IUserAdapter()).adapt(person);
        return new ResponseEntity<ApiUser>(user, HttpStatus.OK);
    }
    
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @RequestMapping(value = "user/domains", method = RequestMethod.POST)
    public ResponseEntity<List<ApiDomain>> getDomains(
    		@ApiParam(value = "EMail", required = true) @RequestBody ApiValue email) {
    	
        TrafficPass pass = null;
        
        try {
            pass = ServiceLocator.getService(TrafficControlService.class).getPass();
            List<ApiDomain> domains = (new ListAdapter<Domain, ApiDomain>(new IDomainAdapter())).adapt(getApiDao().getDomains(email.getName()));
            return new ResponseEntity<List<ApiDomain>>(domains, HttpStatus.OK);
        } 
        catch (Exception e) {
        	kblogger.error(e);
            logger.error(e);
            throw e;
        } 
        finally {
            if (pass != null)  ServiceLocator.getService(TrafficControlService.class).release(pass);
        }
    }
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @RequestMapping(value = "user/domains/{name}", method = RequestMethod.GET)
    public ResponseEntity<ApiDomain> getDomain(
            @ApiParam(value = "name", required = true) @PathVariable("name") String name) {
        
        try {
            Domain domain = getApiDao().findDomainByName(name);
            if (domain==null) {
                throw new ApiException(HttpStatus.NOT_FOUND, ApiError.DOMAIN_NOT_FOUND);
            }
            ApiDomain idomain = (new IDomainAdapter()).adapt(domain);
            return new ResponseEntity<ApiDomain>(idomain, HttpStatus.OK);
        }
        catch (Exception e) {
        	kblogger.error(e);
            logger.error(e);
            throw e;
        } 
    }
    
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @RequestMapping(value = "user/domains/{name}/certificate", method = RequestMethod.GET)
    public ResponseEntity<ICertificate> getCertificate(
            @ApiParam(value = "name", required = true) @PathVariable("name") String name) {
        
        try {
            Domain domain = getApiDao().findDomainByName(name);
            if (domain==null) {
                throw new ApiException(HttpStatus.NOT_FOUND, ApiError.DOMAIN_NOT_FOUND);
            }
            Certificate certificate = domain.getService(DomainService.class).getCertificate();
            ICertificate icertificate = (new ICertificateAdapter()).adapt(certificate);
            return new ResponseEntity<ICertificate>(icertificate, HttpStatus.OK);
        }
        catch (Exception e) {
        	kblogger.error(e);
            logger.error(e);
            throw e;
        } 
    }

    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Get Devices",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found"),
    })
    	
    @RequestMapping(value = "/{domain}/security/devices", method = RequestMethod.GET)
    public ResponseEntity<List<IDevice>> getDevices(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domain) {
    	ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsIn().mark();
        su(domain);
        List<IDevice> devices = (new ListAdapter<UserDevice, IDevice>(new IDeviceAdapter())).adapt(getApiDao().getDevices());
        return new ResponseEntity<List<IDevice>>(devices, HttpStatus.OK);
    }


    /**
     * ------------------------------------------------------------------------------------------------------------------------
     * SECURITY GROUPS
     */
    @ApiOperation(
            value = "List Groups",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1100 - No Data"),
    })
    @RequestMapping(value = "/{domain}/groups", method = RequestMethod.GET)
    public ResponseEntity<IResponse<ApiProxy>> getGroups(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domainname,
            @ApiParam(value = "Iql criteria", required = false) @RequestParam("s") Optional<String> criteria,
            @ApiParam(value = "Offset", required = false) @RequestParam("offset") Optional<Long> offset,
            @ApiParam(value = "Page size", required = false) @RequestParam("pageSize") Optional<Integer> pageSize) {

        ResultSet resultSet = null;

        try {
            ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();

            su(domainname);

            if (!userIs(KbeeGlobalRole.DOMAIN_ADMIN) && !userIs(KbeeGlobalRole.SECURITY)) {
                throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
            }

            String criteriavalue = criteria.isPresent() ? criteria.get().toString() : null;

            resultSet = getApiDao().getGroups(getDomain(), criteriavalue);

            if (offset.isPresent() && offset.get().intValue() > 0) {
                resultSet.absolute(offset.get().intValue());
            }

            int pageSizeValue = 25;
            if (pageSize.isPresent()) {
                pageSizeValue = pageSize.get();
            }

            IResponse<ApiProxy> iResultSet = (new ResultSetAdapter<Group, ApiProxy>(new IProxyAdapter<Group>("group"), pageSizeValue)).adapt(resultSet);

            if (offset.isPresent()) {
                iResultSet.setOffset(offset.get());
            }

            return new ResponseEntity<IResponse<ApiProxy>>(iResultSet, HttpStatus.OK);
        } finally {
            if (resultSet != null)
                resultSet.close();
        }
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Get Group",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1280 - Group not found")})
    @RequestMapping(value = "{domain}/groups/{id}", method = RequestMethod.GET)
    public ResponseEntity<IGroup> getGroup(
            @ApiParam(value = "Domain name", required = true) @PathVariable String domain,
            @ApiParam(value = "Group id", required = true) @PathVariable Long id) {
        su(domain);
        Group group = getApiDao().findGroupById(id);
        IGroup igroup = (new IGroupAdapter()).adapt(group);
        return new ResponseEntity<IGroup>(igroup, HttpStatus.OK);
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     * SECURITY ROLES
     */
    @ApiOperation(
            value = "List Roles",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1100 - No Data"),
    })
    @RequestMapping(value = "/{domain}/roles", method = RequestMethod.GET)
    public ResponseEntity<IResponse<ApiProxy>> getRoles(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domainname,
            @ApiParam(value = "Offset", required = false) @RequestParam("offset") Optional<Long> offset,
            @ApiParam(value = "Page size", required = false) @RequestParam("pageSize") Optional<Integer> pageSize,
            @ApiParam(value = "Is Api", required = false) @RequestParam("isApi") Optional<Boolean> isApi) {

        ResultSet resultSet = null;

        try {
            ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();

            su(domainname);

            Boolean isapi = isApi.isPresent() ? isApi.get() : null;

            if (!userIs(KbeeGlobalRole.DOMAIN_ADMIN) && !userIs(KbeeGlobalRole.SECURITY)) {
                throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
            }

            resultSet = getApiDao().getRoles(getDomain(), isapi);

            if (offset.isPresent() && offset.get().intValue() > 0) {
                resultSet.absolute(offset.get().intValue());
            }

            int pageSizeValue = 25;
            if (pageSize.isPresent()) {
                pageSizeValue = pageSize.get();
            }

            IResponse<ApiProxy> iResultSet = (new ResultSetAdapter<Role, ApiProxy>(new IProxyAdapter<Role>("role"), pageSizeValue)).adapt(resultSet);

            if (offset.isPresent()) {
                iResultSet.setOffset(offset.get());
            }

            return new ResponseEntity<IResponse<ApiProxy>>(iResultSet, HttpStatus.OK);
        } finally {
            if (resultSet != null)
                resultSet.close();
        }
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Get Role",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1300 - Role not found")})
    @RequestMapping(value = "{domain}/roles/{id}", method = RequestMethod.GET)
    public ResponseEntity<IRole> getRole(
            @ApiParam(value = "Domain name", required = true) @PathVariable String domain,
            @ApiParam(value = "Role id", required = true) @PathVariable Long id) {
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        su(domain);
        if (!userIs(KbeeGlobalRole.DOMAIN_ADMIN) && !userIs(KbeeGlobalRole.SECURITY)) {
            throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
        }
        Role role = getApiDao().findRoleById(id);
        if (!role.getDomain().equals(getDomain())) {
            throw new ApiException(HttpStatus.NOT_FOUND, ApiError.ROLE_NOT_FOUND);
        }
        IRole irole = (new IRoleAdapter()).adapt(role);
        return new ResponseEntity<IRole>(irole, HttpStatus.OK);
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     * SECURITY RULES
     */
    @ApiOperation(
            value = "List Rules",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1100 - No Data"),
    })
    @RequestMapping(value = "/{domain}/security/rules", method = RequestMethod.GET)
    public ResponseEntity<IResponse<ApiProxy>> getSecurityRules(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domainname,
            @ApiParam(value = "Offset", required = false) @RequestParam("offset") Optional<Long> offset,
            @ApiParam(value = "Page size", required = false) @RequestParam("pageSize") Optional<Integer> pageSize) {

        ResultSet resultSet = null;

        try {
            su(domainname);

            resultSet = getApiDao().getSecurityRules(getDomain());

            if (offset.isPresent()) {
                resultSet.absolute(offset.get().intValue());
            }

            int pageSizeValue = 25;
            if (pageSize.isPresent()) {
                pageSizeValue = pageSize.get();
            }

            IResponse<ApiProxy> iResultSet = (new ResultSetAdapter<SecurityRule, ApiProxy>(new IProxyAdapter<SecurityRule>("rule"), pageSizeValue)).adapt(resultSet);

            if (offset.isPresent()) {
                iResultSet.setOffset(offset.get());
            }

            return new ResponseEntity<IResponse<ApiProxy>>(iResultSet, HttpStatus.OK);
        } finally {
            if (resultSet != null)
                resultSet.close();
        }
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     * SECURITY RULE
     */
    @ApiOperation(
            value = "Get Rule",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1290 - Rule not found")})
    @RequestMapping(value = "{domain}/security/rules/{id}", method = RequestMethod.GET)
    public ResponseEntity<ISecurityRule> getSecurityRule(
            @ApiParam(value = "Domain name", required = true) @PathVariable String domain,
            @ApiParam(value = "Rule id", required = true) @PathVariable Long id) {
        su(domain);
        SecurityRule rule = getApiDao().findSecurityRuleById(id);
        ISecurityRule irule = (new ISecurityRuleAdapter()).adapt(rule);
        return new ResponseEntity<ISecurityRule>(irule, HttpStatus.OK);
    }

	/**
	* ------------------------------------------------------------------------------------------------------------------------
	* FILES
	*/
    @ApiOperation(
            value = "Adds a new external iDoc Object, or replaces it if it exists.",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 208, message = "1030 - Invalid version"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1210 - File not found"),
            @ApiResponse(code = 429, message = "1020 - Too many requests"),
            @ApiResponse(code = 400, message = "1150 - Invalid application"),
            @ApiResponse(code = 423, message = "1040 - Locked. The file is already in process"),
            @ApiResponse(code = 412, message = "1250 - Class not found\n1400 - Invalid attribute name 'name'. Seeded attribute not found in file class\n1440 - Invalid relation name\n1420 - Attribute 'name' is required\n1410 - Invalid multiplicity for 'name'"),
    })
    
    @RequestMapping(value = "/file/{application}/{domain}/{document}", method = RequestMethod.POST)
    public ResponseEntity<ITransaction> updateFile(
            @ApiParam(value = "Application name", required = true) @PathVariable("application") String application,
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domain,
            @ApiParam(value = "Document id", required = false) @PathVariable("document") String document,
            @ApiParam(value = "Value", required = true) @RequestBody ApiFile file) {

        TrafficPass pass = null;

        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsIn().mark();

        FileUpdateEvent logevent = new FileUpdateEvent(file);

        if (file.getLastModifiedDate() == null) {
            file.setLastModifiedDate(OffsetDateTime.now());
        }

        logevent.setSource(WEB);

        try {
            Session.setApi(true);
            pass = ServiceLocator.getService(TrafficControlService.class).getPass();
            ITransaction transaction = ((FileUpdateAbstractHandler) getHandler(file, "file-update")).update(file);
            logevent.setResponse(transaction);
            logevent.setContentClass(file.getClassName());
            logger.info(logevent);
            return new ResponseEntity<ITransaction>(transaction, HttpStatus.OK);
        } catch (ApiException e) {
            logevent.setResponse(new IError(e.getErrorCode(), e.getMessage()));
            logevent.setStatus(e.getHttpStatus());
            logger.info(logevent);
            throw e;
        } catch (TimeoutException e) {
            logevent.setResponse(new IError(ApiError.TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS"));
            logevent.setStatus(HttpStatus.TOO_MANY_REQUESTS);
            logger.info(logevent);
            throw e;
        } 
        catch (Exception e) {
            logger.error(e);
            throw e;
        } 
        finally {
            if (pass != null)
                ServiceLocator.getService(TrafficControlService.class).release(pass);
            ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
            ServiceLocator.getService(SystemMetricsService.class).getRequestProcessingTimeEstimator().addValue(logevent.getProcessingTime());
            Session.setApi(false);
        }
    }

    @ApiOperation(
            value = "Create a new iDoc Object",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 208, message = "1030 - Invalid version"),
            @ApiResponse(code = 404, message = "1210 - File not found"),
            @ApiResponse(code = 429, message = "1020 - Too many requests"),
            @ApiResponse(code = 423, message = "1040 - Locked. The file is already in process"),
            @ApiResponse(code = 412, message = "1250 - Class not found\n1400 - Invalid attribute name 'name'. Seeded attribute not found in file class\n1440 - Invalid relation name\n1420 - Attribute 'name' is required\n1410 - Invalid multiplicity for 'name'"),
    })
    
    @RequestMapping(value = "/file/new", method = RequestMethod.POST)
    public ResponseEntity<ITransaction> createFile(
            @ApiParam(value = "Value", required = true) @RequestBody ApiFile file) {
        return updateFile(null, null, file);
    }
    
    @ApiOperation(
            value = "Adds a new iDoc Object, or replaces it if it exists.",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 208, message = "1030 - Invalid version"),
            @ApiResponse(code = 404, message = "1210 - File not found"),
            @ApiResponse(code = 429, message = "1020 - Too many requests"),
            @ApiResponse(code = 423, message = "1040 - Locked. The file is already in process"),
            @ApiResponse(code = 412, message = "1250 - Class not found\n1400 - Invalid attribute name 'name'. Seeded attribute not found in file class\n1440 - Invalid relation name\n1420 - Attribute 'name' is required\n1410 - Invalid multiplicity for 'name'"),
    })
    @RequestMapping(value = "/file/{oid}/{id}", method = RequestMethod.POST)
    public ResponseEntity<ITransaction> updateFile(
    		@PathVariable("oid") Long oid, @PathVariable("id") Long id,
            @ApiParam(value = "Value", required = true) @RequestBody ApiFile file) {

         TrafficPass pass = null;
         
         if (file.getDomain()==null) {
         	file.setDomain(getDomain().getName());
         }

        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsIn().mark();
        

        FileUpdateEvent logevent = new FileUpdateEvent(file);

        if (file.getLastModifiedDate() == null) {
            file.setLastModifiedDate(OffsetDateTime.now());
        }

        logevent.setSource(WEB);

        try {
            Session.setApi(true);
            pass = ServiceLocator.getService(TrafficControlService.class).getPass();
            ITransaction transaction = ((FileUpdateAbstractHandler) getHandler(file, "file-update")).update(file);
            logevent.setResponse(transaction);
            logevent.setContentClass(file.getClassName());
            logger.info(logevent);
            return new ResponseEntity<ITransaction>(transaction, HttpStatus.OK);
        } catch (ApiException e) {
            logevent.setResponse(new IError(e.getErrorCode(), e.getMessage()));
            logevent.setStatus(e.getHttpStatus());
            logger.info(logevent);
            throw e;
        } catch (TimeoutException e) {
            logevent.setResponse(new IError(ApiError.TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS"));
            logevent.setStatus(HttpStatus.TOO_MANY_REQUESTS);
            logger.info(logevent);
            throw e;
        } 
        catch (Exception e) {
            logger.error(e);
            throw e;
        } 
        finally {
            if (pass != null)
                ServiceLocator.getService(TrafficControlService.class).release(pass);
            ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
            ServiceLocator.getService(SystemMetricsService.class).getRequestProcessingTimeEstimator().addValue(logevent.getProcessingTime());
            Session.setApi(false);
        }
    }
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Get File",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1210 - File not found"),
            @ApiResponse(code = 400, message = "1150 - Invalid application"),
    })
    @RequestMapping(value = "/file/{application}/{domain}/{document}", method = RequestMethod.GET)
    public ResponseEntity<ApiFile> getFile(
            @ApiParam(value = "Application Name", required = true) @PathVariable("application") String application,
            @ApiParam(value = "Domain Name", required = true) @PathVariable("domain") String domainname,
            @ApiParam(value = "Document Id", required = true) @PathVariable("document") String document,
            @ApiParam(value = "Tokens Flag", required = false) @RequestParam("tokens") Optional<Boolean> tokens,
            @ApiParam(value = "Public Url", required = false) @RequestParam("publicurl") Optional<Boolean> publicUrl) {
    	
    	return getFile("0", application, domainname, document, tokens, publicUrl);
    }
    
    @RequestMapping(
    		value = "/{version}/file/{application}/{domain}/{document}", 
    		method = RequestMethod.GET)
    public ResponseEntity<ApiFile> getFile(
            @ApiParam(value = "Api Version Number", required = true) 
            	@PathVariable("version") String version,
            @ApiParam(value = "Application name", required = true) 
            	@PathVariable("application") String application,
            @ApiParam(value = "Domain Name", required = true) 
            	@PathVariable("domain") String domainname,
            @ApiParam(value = "Document Id", required = true) 
            	@PathVariable("document") String document,
            @ApiParam(value = "Tokens Flag", required = false) 
            	@RequestParam("tokens") Optional<Boolean> tokens,
            @ApiParam(value = "Public Url", required = false) 
            	@RequestParam("publicurl") Optional<Boolean> publicUrl) {

        su(domainname);

        Content content = getApiDao().findContentByExternalId(application, document);

        boolean includeSecurityTokens = tokens.isPresent() ? tokens.get() : false;
        boolean includePublicUrl = publicUrl.isPresent() ? tokens.get() : false;
 
        ApiFile file = getFile(version, 
        	content, 
        	ApiViewMode.All, 
        	includeSecurityTokens, 
        	includePublicUrl);

        return new ResponseEntity<ApiFile>(file, HttpStatus.OK);
    }
    
	/**
	* ------------------------------------------------------------------------------------------------------------------------
	*/
    @ApiOperation(
            value = "Get File",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1210 - File not found"),
            @ApiResponse(code = 400, message = "1150 - Invalid application"),
    })
    @RequestMapping(value = "/file/{oid}/{id}", method = RequestMethod.GET)
    public ResponseEntity<ApiFile> getFile(
            @ApiParam(value = "Unique internal document id", required = true) @PathVariable("oid") Long oid,
            @ApiParam(value = "Unique internal document version id", required = true) @PathVariable("id") Long id,
            @ApiParam(value = "Tokens Flag", required = false) @RequestParam("tokens") Optional<Boolean> tokens,
            @ApiParam(value = "View Mode", required = false) @RequestParam("view") Optional<String> viewMode,
            @ApiParam(value = "Public Url", required = false) @RequestParam("publicurl") Optional<Boolean> publicUrl) {

        return getFile("0", oid, id, tokens, viewMode, publicUrl); 
    }
    
   
    @RequestMapping(value = "/{version}/file/{oid}/{id}", method = RequestMethod.GET)
    public ResponseEntity<ApiFile> getFile(
            @ApiParam(value = "Api Version Number", required = true) @PathVariable("version") String version,
            @ApiParam(value = "Unique Internal Document Id", required = true) @PathVariable("oid") Long oid,
            @ApiParam(value = "Unique Internal Document Version Id", required = true) @PathVariable("id") Long id,
            @ApiParam(value = "Tokens Flag", required = false) @RequestParam("tokens") Optional<Boolean> tokens,
            @ApiParam(value = "View Mode", required = false) @RequestParam("view") Optional<String> viewModeParam,
            @ApiParam(value = "Public Url", required = false) @RequestParam("publicurl") Optional<Boolean> publicUrl) {

        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();

        Content content = getApiDao().findContentById(id);

        boolean includeSecurityTokens = tokens.isPresent() ? tokens.get() : false;
        boolean includePublicUrl = publicUrl.isPresent() ? publicUrl.get() : false;
        ApiViewMode viewMode = viewModeParam.isPresent() ? ApiViewMode.valueOf(viewModeParam.get()) : ApiViewMode.All;

        ApiFile file = getFile(version, content, viewMode, includeSecurityTokens, includePublicUrl);

        return new ResponseEntity<ApiFile>(file, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/resource/{id}/metainfo", method = RequestMethod.GET)
    public ResponseEntity<ApiResource> getResource(
            @ApiParam(value = "Id", required = true) @PathVariable("id") Long id) {

        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();

        Resource resource = getApiDao().findResourceById(id);

        ApiResource iresource = (new IResourceAdapter()).adapt(resource);

        return new ResponseEntity<ApiResource>(iresource, HttpStatus.OK);
    }
    
	/**
	* ------------------------------------------------------------------------------------------------------------------------
	* FORMS
	*/
    
    @ApiOperation(
            value = "Update Eform Data",
            authorizations = {@Authorization(value = "basicAuth"), @Authorization(value = "bearerAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1210 - File not found"),
            @ApiResponse(code = 429, message = "1020 - Too many requests"),
            @ApiResponse(code = 423, message = "1040 - Locked. The file is already in process"),
            @ApiResponse(code = 412, message = "1250 - Class not found\n1400"),
    })
    @RequestMapping(value = "/eform/{oid}/{id}/{eform}", method = RequestMethod.POST)
    public ResponseEntity<ITransaction> updateForm(
    		@PathVariable("oid") Long oid, 
    		@PathVariable("id") Long id,
    		@PathVariable("eform") Long eform,
            @ApiParam(value = "Validation", required = false) @RequestParam("validation") Optional<String> validationParameter,
            @ApiParam(value = "value", required = true) @RequestBody IFormData data) {
    	
    	TrafficPass pass = null;
        try {
        	Session.setApi(true);
        	pass = ServiceLocator.getService(TrafficControlService.class).getPass();
        	boolean validation = true;
            if (validationParameter.isPresent()) {
                validation = !"false".equals(validationParameter.get());
            }
        	ITransaction transaction = ((FileFormUpdateHandler) getHandler("eform-update")).update(data, validation);
        	return new ResponseEntity<ITransaction>(transaction, HttpStatus.OK);
        } 
        catch (ApiException e) {
        	throw e;
        } 
        catch (TimeoutException e) {
        	kblogger.error(e);
        	throw e;
        } 
        catch (Exception e) {
        	kblogger.error(e);
        	throw e;
        } 
        finally {
        	if (pass != null) {
               ServiceLocator.getService(TrafficControlService.class).release(pass);
        	}    
        	ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
        	Session.setApi(false);
       }
    }
    
    @ApiOperation(
            value = "Update Value Eform Data",
            authorizations = {@Authorization(value = "basicAuth"), @Authorization(value = "bearerAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1210 - File not found"),
            @ApiResponse(code = 429, message = "1020 - Too many requests"),
            @ApiResponse(code = 423, message = "1040 - Locked. The file is already in process"),
            @ApiResponse(code = 412, message = "1250 - Class not found\n1400"),
    })
    @RequestMapping(value = "/valueform/{id}", method = RequestMethod.POST)
    public ResponseEntity<ITransaction> updateValueForm(
    		@PathVariable("id") Long id,
            @ApiParam(value = "value", required = true) @RequestBody IFormData data) {
    	
    	TrafficPass pass = null;
        try {
        	Session.setApi(true);
        	pass = ServiceLocator.getService(TrafficControlService.class).getPass();
        	ITransaction transaction = ((ValueFormUpdateHandler) getHandler("value-eform-update")).update(data);
        	return new ResponseEntity<ITransaction>(transaction, HttpStatus.OK);
        } 
        catch (ApiException e) {
        	throw e;
        } 
        catch (TimeoutException e) {
        	kblogger.error(e);
        	throw e;
        } 
        catch (Exception e) {
        	kblogger.error(e);
        	throw e;
        } 
        finally {
        	if (pass != null) {
               ServiceLocator.getService(TrafficControlService.class).release(pass);
        	}    
        	ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
        	Session.setApi(false);
       }
    }

    
    @ApiOperation(
            value = "Content EForm Field Options Values",
            authorizations = {@Authorization(value = "basicAuth"), @Authorization(value = "bearerAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 404, message = "1210 - File not found"),
            @ApiResponse(code = 429, message = "1020 - Too many requests")
    })
    @RequestMapping(value = "/file/{oid}/{id}/{eform}/{field}/options", method = RequestMethod.GET)
    public ResponseEntity<List<IFieldValue>> getEFormFieldOptions(
    		@PathVariable("oid") Long oid, 
    		@PathVariable("id") Long id,
    		@PathVariable("eform") Long eform,
    		@PathVariable("field") String field,
    		@ApiParam(value = "pattern", required = false) @RequestParam("pattern") Optional<String> pattern) {
    	
    	TrafficPass pass = null;

    	ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsIn().mark();

        try {
           pass = ServiceLocator.getService(TrafficControlService.class).getPass();
           String patternvalue = pattern.isPresent() ? pattern.get() : null;
           List<IFieldValue> options = ((FileFormFieldOptionsHandler) getHandler("eform-field-options")).getOptions(id, eform, field, patternvalue);
           return new ResponseEntity<List<IFieldValue>>(options, HttpStatus.OK);
        } 
        catch (Exception e) {
        	kblogger.error(e);
        	throw e;
        } 
        finally {
        	if (pass != null) {
               ServiceLocator.getService(TrafficControlService.class).release(pass);
        	}    
        	ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
        }
    }
    
    @ApiOperation(
            value = "Value EForm Field Options Values",
            authorizations = {@Authorization(value = "basicAuth"), @Authorization(value = "bearerAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 404, message = "1210 - File not found")
    })
    @RequestMapping(value = "/valueform/{id}/{field}/options", method = RequestMethod.GET)
    public ResponseEntity<List<IFieldValue>> getEValueFormFieldOptions(
    		@PathVariable("id") Long id,
    		@PathVariable("field") String field,
    		@ApiParam(value = "pattern", required = false) @RequestParam("pattern") Optional<String> pattern) {
    	
    	TrafficPass pass = null;

    	ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsIn().mark();

        try {
           pass = ServiceLocator.getService(TrafficControlService.class).getPass();
           String patternvalue = pattern.isPresent() ? pattern.get() : null;
           List<IFieldValue> options = ((ValueFormFieldOptionsHandler) getHandler("value-eform-field-options")).getOptions(id, field, patternvalue);
           return new ResponseEntity<List<IFieldValue>>(options, HttpStatus.OK);
        } 
        catch (Exception e) {
        	kblogger.error(e);
        	throw e;
        } 
        finally {
        	if (pass != null) {
               ServiceLocator.getService(TrafficControlService.class).release(pass);
        	}    
        	ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
        }
    }

    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Procedure Launch",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found"),
            @ApiResponse(code = 404, message = "1210 - Procedure not found"),
    })
    @RequestMapping(value = "/file/{application}/{domain}/{procedure}/new", method = RequestMethod.POST)
    public ResponseEntity<ITransaction> startProcess(
            @ApiParam(value = "Application name", required = true) @PathVariable("application") String application,
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domain,
            @ApiParam(value = "Procedure", required = false) @PathVariable("procedure") String procedure,
            @ApiParam(value = "Value", required = true) @RequestBody ApiFile file) {
    	
        TrafficPass pass = null;

        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsIn().mark();

        FileUpdateEvent logevent = new FileUpdateEvent(file);

        if (file.getLastModifiedDate() == null) {
            file.setLastModifiedDate(OffsetDateTime.now());
        }

        logevent.setSource(WEB);
        
        try {
            Session.setApi(true);
            pass = ServiceLocator.getService(TrafficControlService.class).getPass();
            ITransaction transaction = ((ProcessLaunchHandler) getHandler(file, "process-launch")).launch(procedure, file);
            logevent.setResponse(transaction);
            logevent.setContentClass(file.getClassName());
            logger.info(logevent);
            return new ResponseEntity<ITransaction>(transaction, HttpStatus.OK);
        } catch (ApiException e) {
            logevent.setResponse(new IError(e.getErrorCode(), e.getMessage()));
            logevent.setStatus(e.getHttpStatus());
            logger.info(logevent);
            throw e;
        } catch (TimeoutException e) {
            logevent.setResponse(new IError(ApiError.TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS"));
            logevent.setStatus(HttpStatus.TOO_MANY_REQUESTS);
            logger.info(logevent);
            throw e;
        } 
        catch (Exception e) {
            logger.error(e);
            throw e;
        } 
        finally {
            if (pass != null)
                ServiceLocator.getService(TrafficControlService.class).release(pass);
            ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
            ServiceLocator.getService(SystemMetricsService.class).getRequestProcessingTimeEstimator().addValue(logevent.getProcessingTime());
            Session.setApi(false);
        }
    	
    }
    
    @ApiOperation(
            value = "Launcher execute",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found"),
            @ApiResponse(code = 404, message = "1210 - Launcher not found"),
    })
    @RequestMapping(value = "/file/{domain}/{launcher}/execute", method = RequestMethod.POST)
    public ResponseEntity<ITransaction> launchProcess(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domain,
            @ApiParam(value = "Launcher", required = false) @PathVariable("launcher") String launcher,
            @ApiParam(value = "File", required = true) @RequestBody ApiFile file) {
    	
        TrafficPass pass = null;

        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsIn().mark();

        FileUpdateEvent logevent = new FileUpdateEvent(file);

        if (file.getLastModifiedDate() == null) {
            file.setLastModifiedDate(OffsetDateTime.now());
        }

        logevent.setSource(WEB);
        
        try {
            Session.setApi(true);
            pass = ServiceLocator.getService(TrafficControlService.class).getPass();
            ITransaction transaction = ((ProcessLaunchHandler) getHandler(file, "process-launch")).execute(launcher, file);
            logevent.setResponse(transaction); 
            logevent.setContentClass(file.getClassName());
            logger.info(logevent);
            return new ResponseEntity<ITransaction>(transaction, HttpStatus.OK);
        } catch (ApiException e) {
            logevent.setResponse(new IError(e.getErrorCode(), e.getMessage()));
            logevent.setStatus(e.getHttpStatus());
            logger.info(logevent);
            throw e;
        } catch (TimeoutException e) {
            logevent.setResponse(new IError(ApiError.TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS"));
            logevent.setStatus(HttpStatus.TOO_MANY_REQUESTS);
            logger.info(logevent);
            throw e;
        } 
        catch (Exception e) {
            logger.error(e);
            throw e;
        } 
        finally {
            if (pass != null)
                ServiceLocator.getService(TrafficControlService.class).release(pass);
            ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
            ServiceLocator.getService(SystemMetricsService.class).getRequestProcessingTimeEstimator().addValue(logevent.getProcessingTime());
            Session.setApi(false);
        }
    	
    }

    		


    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Delete File",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1200 - Domain not found\n1210 - File not found"),
    })
    
    @RequestMapping(value = "/file/{application}/{domain}/{document}", method = RequestMethod.DELETE)
    public ResponseEntity<ITransaction> deleteFile(
            @ApiParam(value = "Application name", required = true) @PathVariable("application") String application,
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domainname,
            @ApiParam(value = "Document id", required = true) @PathVariable("document") String document,
            @ApiParam(value = "Permanent Delete Flag", required = false) @RequestParam("permanentDelete") Optional<Boolean> permanentDelete) {

        TrafficPass pass = null;

        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsIn().mark();

        boolean dorecycle = permanentDelete.isPresent() ? !permanentDelete.get() : true;

        FileDeleteEvent logevent = new FileDeleteEvent("/file/" + application + "/" + domainname + "/" + document);

        logevent.setFile(document);
        logevent.setSource(WEB);
        logevent.setDomain(domainname);

        try {
            pass = ServiceLocator.getService(TrafficControlService.class).getPass();

            ServiceLocator.getService(ValueLockerService.class).lock(document);

            su(domainname);

            ITransaction transaction;
            if (dorecycle) {
                transaction = ((FileDeleteHandler) getHandler("file-delete")).recycle(application, document);
            } else {
                transaction = ((FileDeleteHandler) getHandler("file-delete")).delete(application, document);
            }
            logevent.setResponse(transaction);
            logger.info(logevent);
            return new ResponseEntity<ITransaction>(transaction, HttpStatus.OK);
        } catch (ApiException e) {
            logevent.setFile(document);
            logevent.setResponse(new IError(e.getErrorCode(), e.getMessage()));
            logevent.setStatus(e.getHttpStatus());
            if (e.isAuditable())logger.info(logevent);
            throw e;
        } catch (TimeoutException e) {
            logevent.setResponse(new IError(ApiError.TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS"));
            logevent.setStatus(HttpStatus.TOO_MANY_REQUESTS);
            logger.info(logevent);
            throw e;
        } finally {
            ServiceLocator.getService(ValueLockerService.class).unlock(document);
            ServiceLocator.getService(TrafficControlService.class).release(pass);
            ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
            ServiceLocator.getService(SystemMetricsService.class).getRequestProcessingTimeEstimator().addValue(logevent.getProcessingTime());
        }
    }
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @RequestMapping(value = "/file/{oid}/{id}/audit", method = RequestMethod.GET)
    public ResponseEntity<List<ILogEvent>> getFileAudit(@PathVariable("oid") Long oid, @PathVariable("id") Long id) {

        Content content = getApiDao().findContentById(id);

        List<ILogEvent> events = (new ListAdapter<LogEvent, ILogEvent>(new ILogEventAdapter())).adapt(getApiDao().getAudit(content));

        return new ResponseEntity<List<ILogEvent>>(events, HttpStatus.OK);
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @RequestMapping(value = "/file/{oid}/{id}/history", method = RequestMethod.GET)
    public ResponseEntity<List<ApiFile>> getFileHistory(@PathVariable("oid") Long oid, @PathVariable("id") Long id) {

        Content content = getApiDao().findContentById(id);

        getFile(content);

        List<KbeeIDoc> history = new ArrayList<KbeeIDoc>();
        for (Content version : getApiDao().getHistory(content)) {
            if (version instanceof KbeeIDoc) {
                history.add((KbeeIDoc) version);
            }
        }

        List<ApiFile> files = (new ListAdapter<KbeeIDoc, ApiFile>(new IDocAdapter(false))).adapt(history);

        return new ResponseEntity<List<ApiFile>>(files, HttpStatus.OK);
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "List Files",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1100 - No Data"),
            @ApiResponse(code = 400, message = "8500 - Criteria expression error. Predicate error\n8200 - Criteria expression error. Syntax error"),
    })
    @RequestMapping(value = "/file/{domain}/select", method = RequestMethod.GET)
    public ResponseEntity<IResponse<ApiFile>> select(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domainname,
            @ApiParam(value = "Iql criteria", required = false) @RequestParam("s") String statement,
            @ApiParam(value = "Offset", required = false) @RequestParam("offset") Optional<Long> offset,
            @ApiParam(value = "Page size", required = false) @RequestParam("pageSize") Optional<Integer> pageSize,
            @ApiParam(value = "All states", required = false) @RequestParam("allstates") Optional<String> allstatesparameter,
            @ApiParam(value = "CRC", required = false) @RequestParam("crc") Optional<String> crcparameter) {

        ResultSet resultSet = null;

        try {
            ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();

            su(domainname);

            boolean allstates = allstatesparameter.isPresent() && allstatesparameter.get().equals("true");

            resultSet = getApiDao().executeIql(statement, allstates);

            if (offset.isPresent() && offset.get().intValue()>0) {
                resultSet.absolute(offset.get().intValue());
            }

            int pageSizeValue = 25;
            if (pageSize.isPresent()) {
                pageSizeValue = pageSize.get();
                ((SolrResultSet) resultSet).setPageSize(pageSizeValue);
            }

            boolean includecrc = crcparameter.isPresent() && crcparameter.get().equals("true");

            IResponse<ApiFile> iResultSet = (new ResultSetAdapter<KbeeIDoc, ApiFile>(new IDocAdapter(includecrc), pageSizeValue)).adapt(resultSet);

            if (offset.isPresent()) {
                iResultSet.setOffset(offset.get());
            }

            return new ResponseEntity<IResponse<ApiFile>>(iResultSet, HttpStatus.OK);
        } 
        finally {
            if (resultSet != null)
                resultSet.close();
        }
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Delete Files",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 400, message = "8500 - Criteria expression error. Predicate error\n8200 - Criteria expression error. Syntax error"),
    })
    @RequestMapping(value = "/file/{domain}/delete", method = RequestMethod.POST)
    public ResponseEntity<ITransaction> deleteFiles(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domainname,
            @ApiParam(value = "Iql criteria", required = false) @RequestParam("s") String statement,
            @ApiParam(value = "Permanent Delete Flag", required = false) @RequestParam("permanentDelete") Optional<Boolean> permanentDelete) {

        ResultSet resultSet = null;

        boolean dorecycle = permanentDelete.isPresent() ? !permanentDelete.get() : true;

        try {
            su(domainname);

            resultSet = getApiDao().executeIql(statement);

            if (!resultSet.hasNext()) {
                throw new ApiException(HttpStatus.NOT_FOUND, ApiError.NO_DATA);
            }

            ITransaction transaction = ((FileDeleteHandler) getHandler("file-delete")).delete(statement, dorecycle);

            return new ResponseEntity<ITransaction>(transaction, HttpStatus.OK);
        } finally {
            if (resultSet != null)
                resultSet.close();
        }
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     * COMMANDS
     */
    @ApiOperation(
            value = "Get Command",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "1010 - Access denied"),
            @ApiResponse(code = 404, message = "1210 - Command not found\n1100 - No Data"),
    })
    @RequestMapping(value = "/command/{id}", method = RequestMethod.GET)
    public ResponseEntity<ICommand> getCommand(
            @ApiParam(value = "Command id", required = true) @PathVariable("id") Long id) {

        Command command = getApiDao().findCommandById(id);

        ICommand icommand = (new ICommandAdapter()).adapt(command);

        return new ResponseEntity<ICommand>(icommand, HttpStatus.OK);
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     * RESOURCES: ver multipart-config del dispatcher servlet de spring
     */
    @ApiOperation(
            value = "Upload Resource",
            authorizations = {@Authorization(value = "basicAuth")})
    @PostMapping("/resource/upload/{application}/{domain}/{document}")
    public ResponseEntity<ITransaction> uploadResource(
            @ApiParam(value = "Application name", required = true) @PathVariable("application") String application,
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domainname,
            @ApiParam(value = "Document id", required = true) @PathVariable("document") String document,
            @ApiParam(value = "Value", required = true) @RequestParam("file") MultipartFile file) {

        TrafficPass pass = null;

        ResourceUploadEvent logevent = new ResourceUploadEvent("/resource/upload/" + application + "/" + domainname + "/" + document,
                domainname,
                document,
                file.getOriginalFilename());

        try {
            pass = ServiceLocator.getService(TrafficControlService.class).getPass();
            Resource resource = getApiDao().upload(file);
            ITransaction transaction = ServiceLocator.getService(ApiTransactionService.class).getTransaction(new ApiProxy("/resource/" + resource.getId()));
            logevent.setResponse(transaction);
            logger.info(logevent);
            return new ResponseEntity<ITransaction>(transaction, HttpStatus.OK);
        } finally {
            ServiceLocator.getService(TrafficControlService.class).release(pass);
        }
    }
    
    @ApiOperation(
            value = "Upload Single Resource",
            authorizations = {@Authorization(value = "basicAuth")})
    @PostMapping("/binfile/upload")
    public ResponseEntity<ITransaction> uploadFile(
            @ApiParam(value = "Value", required = true) @RequestParam("file") MultipartFile file) {

        TrafficPass pass = null;


        try {
            kblogger.debug("UPLOAD");

            ResourceUploadEvent logevent = new ResourceUploadEvent("/resource/upload",
                    getDomain().getName(),
                    null,
                    file.getOriginalFilename());

            pass = ServiceLocator.getService(TrafficControlService.class).getPass();
            Resource resource = getApiDao().upload(file);
            ApiProxy resourceproxy = new ApiProxy(String.valueOf(resource.getId()), resource.getName(), "/resource/" + resource.getId(), "resource");
            ITransaction transaction = ServiceLocator.getService(ApiTransactionService.class).getTransaction(resourceproxy);
            logevent.setResponse(transaction);
            logger.info(logevent);
            return new ResponseEntity<ITransaction>(transaction, HttpStatus.OK);
        }
        catch (Exception e) {
        	kblogger.error(e);
        	throw e;
        }
        finally {
            ServiceLocator.getService(TrafficControlService.class).release(pass);
        }
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ApiOperation(
            value = "Replicate",
            authorizations = {@Authorization(value = "basicAuth"), @Authorization(value = "bearerAuth")})
    @RequestMapping(value = "/{domain}/replicate/{replicaId}", method = RequestMethod.POST)
    public ResponseEntity<ITransaction> replicate(
            @ApiParam(value = "Domain name", required = true) @PathVariable("domain") String domain,
            @ApiParam(value = "Replica id", required = true) @PathVariable("replicaId") String replicaId,
            @ApiParam(value = "Object", required = true) @RequestBody IObjectReplica replica) {
    	
        ServiceLocator.getService(SystemMetricsService.class).getMeterAPIGet().mark();
        
        try {
        	RestObjectMapper restObjectMapper = new RestObjectMapper();
        	Class<?> iclass = Class.forName(replica.getIclass());
        	ApiObject object = (ApiObject)restObjectMapper.readValue(replica.getData(), iclass);
        	ITransaction transaction = ((ReplicaHandler) getHandler("replica")).replicate(object, replicaId);
            return new ResponseEntity<ITransaction>(transaction, HttpStatus.OK);
        }
        catch (ApiException e) {
            throw e;
        } 
        catch (Exception e) {
        	logger.error(e);
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.INTERNAL_ERROR);
        }
        finally {
        	ServiceLocator.getService(SystemMetricsService.class).getMeterAPIRequestsOut().mark();
        }
    }
    
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<IError> handleException(ApiException e) {
        return new ResponseEntity<IError>(new IError(e.getErrorCode(), e.getMessage()), e.getHttpStatus());
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ExceptionHandler(IndexerException.class)
    public ResponseEntity<IError> handleException(IndexerException e) {
    	kblogger.error(e);
        if (e instanceof PredicateNotFoundException) {
            return new ResponseEntity<IError>(new IError(ApiError.IQL_PREDICATE_ERROR, e.getMessage()), HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<IError>(new IError(ApiError.INTERNAL_ERROR, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<IError> handleException(TimeoutException e) {
    	kblogger.error(e);
        return new ResponseEntity<IError>(new IError(ApiError.TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS"), HttpStatus.TOO_MANY_REQUESTS);
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    @ExceptionHandler(ParserException.class)
    public ResponseEntity<IError> handleException(ParserException e) {
    	kblogger.error(e);
        return new ResponseEntity<IError>(new IError(ApiError.IQL_SYNTAX_ERROR, e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    private ApiFile getFile(Content content) {
        return getFile(null, content, ApiViewMode.All, false, false);
    }

    private ApiFile getFile(String version, 
    	Content content,
    	ApiViewMode viewMode,
    	boolean includesecuritytokens, 
    	boolean includeurl) {
    	
        if (content == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, ApiError.FILE_NOT_FOUND);
        }
        
        boolean access = false;
        if (!getDomain().getName().equals(content.getDomain().getName())) {
            if ("kbee".equals(getDomain().getName())) {
                access = su(content.getDomain()) && isReadable(content);
            }
        } 
        else {
            access = isReadable(content);
        }

        if (!access) {
            throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
        }

        ApiFile file = (new IDocAdapter(version, 
        	viewMode, 
        	false, 
        	includesecuritytokens, 
        	includeurl))
        	.adapt((KbeeIDoc) content);

        return file;
    }

    /**
     * 
     * 
     * 
     */
    private ApiUser getUser(Person person) {

        if (person == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, ApiError.USER_NOT_FOUND);
        }

        if (!getDomain().getName().equals(person.getDomain().getName())) {
            throw new ApiException(HttpStatus.NOT_FOUND, ApiError.USER_NOT_FOUND);
        }

        ApiUser user = (new IUserAdapter()).adapt(person);

        return user;
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    protected void su(String domainname) {
        Domain domain = getApiDao().findDomainByName(domainname);
        if (!domain.equals(getDomain())) {
            if ("kbee".equals(getDomain().getName())) {
                su(domain);
            } else {
                throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
            }
        }
        if (!domain.isAPIEnabled()) {
            throw new ApiException(HttpStatus.FORBIDDEN, ApiError.API_NOT_ENABLED);
        }
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    private boolean su(Domain domain) {
        try {
            if (!getDomain().equals(domain)) {
                String username = getUser().getName();
                int i = username.indexOf("@");
                if (i < 0) i = username.length();
                username = username.substring(0, i);
                String suusername = username + "@" + domain.getName();
                ServiceLocator.getService(SecurityService.class).authenticate(suusername);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    private RequestHandler getHandler(String handlername) {
        return (RequestHandler) ServiceLocator.getService(BeansService.class).getBean(handlername + "-handler");
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */

    private Set<String> notHandledAppliactions = new HashSet<String>();
    private Set<String> notHandledClass = new HashSet<String>();

    private RequestHandler getHandler(ApiFile file, String handlername) {
        RequestHandler handler = null;
        if (file.getApplication() != null && file.getClassName() != null) {
            String classhandler = file.getApplication() + "-" + file.getClassName().toLowerCase() + "-" + handlername + "-handler";
            if (!notHandledClass.contains(classhandler)) {
                try {
                    handler = (RequestHandler) ServiceLocator.getService(BeansService.class).getBean(classhandler);
                } catch (Exception e) {
                }
                if (handler == null) {
                    notHandledClass.add(classhandler);
                }
            }
        }
        if (handler == null && file.getApplication() != null && !notHandledAppliactions.contains(file.getApplication())) {
            try {
                handler = (RequestHandler) ServiceLocator.getService(BeansService.class).getBean(file.getApplication() + "-" + handlername + "-handler");
            } catch (Exception e) {
            }
            if (handler == null) {
                notHandledAppliactions.add(file.getApplication());
            }
        }
        if (handler == null) {
            handler = (RequestHandler) ServiceLocator.getService(BeansService.class).getBean(handlername + "-handler");
        }
        return handler;
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    private boolean userIs(KbeeGlobalRole group) {
        return ServiceLocator.getService(SecurityService.class).isMember(group.getId());
    }
    
    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    private void checkOrAddDevice(IDevice idevice) {
    	checkDevice(idevice, true);
    }
    
//    private void checkDevice(IDevice idevice) {
//    	checkDevice(idevice, false);
//    }
    
    private void checkDevice(String deviceId) {
    	UserDevice device = getDevice(deviceId);
    	if (device == null) {
    		if (getDomain().getSecurityLevel()>1) {
    			throw new ApiException(HttpStatus.FORBIDDEN, ApiError.DEVICE_NOT_REGISTERED);
    		}
    	}	
    	else {
    		if (!ObjectState.ENABLED.equals(device.getState())) {
    			throw new ApiException(HttpStatus.FORBIDDEN, ApiError.DEVICE_NOT_REGISTERED);
    		}
    	}
    }
    
    private void checkDevice(IDevice idevice, boolean addIfNotExist) {
    	UserDevice device = getDevice(idevice.getId());
    	if (device == null) {
    		if (getDomain().getSecurityLevel()>1) {
    			throw new ApiException(HttpStatus.FORBIDDEN, ApiError.DEVICE_NOT_REGISTERED);
    		}
    		else {
    			if (addIfNotExist) {
    				((DeviceUpdateHandler) getHandler("device-add")).add(idevice);
    			}
    		}
    	}
    	else {
    		if (!ObjectState.ENABLED.equals(device.getState())) {
    			throw new ApiException(HttpStatus.FORBIDDEN, ApiError.DEVICE_NOT_REGISTERED);
    		}
    	}
    }

    private UserDevice getDevice(String deviceId) {
        for (UserDevice device : ServiceLocator.getService(UserService.class).getSessionUserProfile().getDevices()) {
        	if (device.getDeviceId().equals(deviceId)) {
        		return device;
        	}
        }
        return null;
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    private boolean isReadable(Content content) {
        return ServiceLocator.getService(ContentSystemSecurityService.class).isReadable(content);
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    private ApiDao getApiDao() {
        return (ApiDao) ServiceLocator.getService(BeansService.class).getBean("apiDao");
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    protected Domain getDomain() {
        return ServiceLocator.getService(UserService.class).getDomain();
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------
     */
    private User getUser() {
        return ServiceLocator.getService(SecurityService.class).getSessionUser();
    }
}