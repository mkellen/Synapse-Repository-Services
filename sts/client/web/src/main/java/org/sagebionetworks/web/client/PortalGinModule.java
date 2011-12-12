package org.sagebionetworks.web.client;

import org.sagebionetworks.gwt.client.schema.adapter.JSONArrayGwt;
import org.sagebionetworks.gwt.client.schema.adapter.JSONObjectGwt;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.cookie.GWTCookieImpl;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.security.AuthenticationControllerImpl;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.transform.NodeModelCreatorImpl;
import org.sagebionetworks.web.client.view.AnalysesHomeView;
import org.sagebionetworks.web.client.view.AnalysesHomeViewImpl;
import org.sagebionetworks.web.client.view.AnalysisView;
import org.sagebionetworks.web.client.view.AnalysisViewImpl;
import org.sagebionetworks.web.client.view.CellTableProvider;
import org.sagebionetworks.web.client.view.CellTableProviderImpl;
import org.sagebionetworks.web.client.view.ColumnsPopupView;
import org.sagebionetworks.web.client.view.ColumnsPopupViewImpl;
import org.sagebionetworks.web.client.view.ComingSoonView;
import org.sagebionetworks.web.client.view.ComingSoonViewImpl;
import org.sagebionetworks.web.client.view.DatasetView;
import org.sagebionetworks.web.client.view.DatasetViewImpl;
import org.sagebionetworks.web.client.view.DatasetsHomeView;
import org.sagebionetworks.web.client.view.DatasetsHomeViewImpl;
import org.sagebionetworks.web.client.view.EntityView;
import org.sagebionetworks.web.client.view.EntityViewImpl;
import org.sagebionetworks.web.client.view.HomeView;
import org.sagebionetworks.web.client.view.HomeViewImpl;
import org.sagebionetworks.web.client.view.LayerView;
import org.sagebionetworks.web.client.view.LayerViewImpl;
import org.sagebionetworks.web.client.view.LoginView;
import org.sagebionetworks.web.client.view.LoginViewImpl;
import org.sagebionetworks.web.client.view.LookupView;
import org.sagebionetworks.web.client.view.LookupViewImpl;
import org.sagebionetworks.web.client.view.PhenoEditView;
import org.sagebionetworks.web.client.view.PhenoEditViewImpl;
import org.sagebionetworks.web.client.view.ProfileView;
import org.sagebionetworks.web.client.view.ProfileViewImpl;
import org.sagebionetworks.web.client.view.ProjectView;
import org.sagebionetworks.web.client.view.ProjectViewImpl;
import org.sagebionetworks.web.client.view.ProjectsHomeView;
import org.sagebionetworks.web.client.view.ProjectsHomeViewImpl;
import org.sagebionetworks.web.client.view.StepView;
import org.sagebionetworks.web.client.view.StepViewImpl;
import org.sagebionetworks.web.client.view.StepsHomeView;
import org.sagebionetworks.web.client.view.StepsHomeViewImpl;
import org.sagebionetworks.web.client.view.table.ColumnFactory;
import org.sagebionetworks.web.client.view.table.ColumnFactoryImpl;
import org.sagebionetworks.web.client.view.users.PasswordResetView;
import org.sagebionetworks.web.client.view.users.PasswordResetViewImpl;
import org.sagebionetworks.web.client.view.users.RegisterAccountView;
import org.sagebionetworks.web.client.view.users.RegisterAccountViewImpl;
import org.sagebionetworks.web.client.widget.breadcrumb.BreadcrumbView;
import org.sagebionetworks.web.client.widget.breadcrumb.BreadcrumbViewImpl;
import org.sagebionetworks.web.client.widget.editpanels.AnnotationEditorView;
import org.sagebionetworks.web.client.widget.editpanels.AnnotationEditorViewImpl;
import org.sagebionetworks.web.client.widget.editpanels.NodeEditorView;
import org.sagebionetworks.web.client.widget.editpanels.NodeEditorViewImpl;
import org.sagebionetworks.web.client.widget.editpanels.phenotype.ColumnDefinitionEditorView;
import org.sagebionetworks.web.client.widget.editpanels.phenotype.ColumnDefinitionEditorViewImpl;
import org.sagebionetworks.web.client.widget.editpanels.phenotype.ColumnMappingEditorView;
import org.sagebionetworks.web.client.widget.editpanels.phenotype.ColumnMappingEditorViewImpl;
import org.sagebionetworks.web.client.widget.editpanels.phenotype.OntologySearchPanelView;
import org.sagebionetworks.web.client.widget.editpanels.phenotype.OntologySearchPanelViewImpl;
import org.sagebionetworks.web.client.widget.editpanels.phenotype.PhenotypeEditorView;
import org.sagebionetworks.web.client.widget.editpanels.phenotype.PhenotypeEditorViewImpl;
import org.sagebionetworks.web.client.widget.editpanels.phenotype.PhenotypeMatrixView;
import org.sagebionetworks.web.client.widget.editpanels.phenotype.PhenotypeMatrixViewImpl;
import org.sagebionetworks.web.client.widget.entity.EntityPageTopView;
import org.sagebionetworks.web.client.widget.entity.EntityPageTopViewImpl;
import org.sagebionetworks.web.client.widget.entity.menu.ActionMenuView;
import org.sagebionetworks.web.client.widget.entity.menu.ActionMenuViewImpl;
import org.sagebionetworks.web.client.widget.filter.QueryFilterView;
import org.sagebionetworks.web.client.widget.filter.QueryFilterViewImpl;
import org.sagebionetworks.web.client.widget.footer.FooterView;
import org.sagebionetworks.web.client.widget.footer.FooterViewImpl;
import org.sagebionetworks.web.client.widget.header.HeaderView;
import org.sagebionetworks.web.client.widget.header.HeaderViewImpl;
import org.sagebionetworks.web.client.widget.licenseddownloader.LicensedDownloaderView;
import org.sagebionetworks.web.client.widget.licenseddownloader.LicensedDownloaderViewImpl;
import org.sagebionetworks.web.client.widget.login.LoginWidgetView;
import org.sagebionetworks.web.client.widget.login.LoginWidgetViewImpl;
import org.sagebionetworks.web.client.widget.modal.ModalWindowView;
import org.sagebionetworks.web.client.widget.modal.ModalWindowViewImpl;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditorView;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditorViewImpl;
import org.sagebionetworks.web.client.widget.sharing.AccessMenuButtonView;
import org.sagebionetworks.web.client.widget.sharing.AccessMenuButtonViewImpl;
import org.sagebionetworks.web.client.widget.statictable.StaticTableView;
import org.sagebionetworks.web.client.widget.statictable.StaticTableViewImpl;
import org.sagebionetworks.web.client.widget.table.QueryServiceTableView;
import org.sagebionetworks.web.client.widget.table.QueryServiceTableViewGxtImpl;

import com.google.gwt.cell.client.widget.CustomWidgetImageBundle;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

public class PortalGinModule extends AbstractGinModule {

	@Override
	protected void configure() {
		
		// AuthenticationController
		bind(AuthenticationControllerImpl.class).in(Singleton.class);
		bind(AuthenticationController.class).to(AuthenticationControllerImpl.class);

		// GlobalApplicationState
		bind(GlobalApplicationStateImpl.class).in(Singleton.class);
		bind(GlobalApplicationState.class).to(GlobalApplicationStateImpl.class);
		
		// Header & Footer
		bind(HeaderView.class).to(HeaderViewImpl.class);
		bind(FooterView.class).to(FooterViewImpl.class);

		// EntityType
		bind(EntityTypeProvider.class).in(Singleton.class);
		
		// JSONAdapters
		bind(JSONObjectAdapter.class).to(JSONObjectGwt.class);
		bind(JSONArrayAdapter.class).to(JSONArrayGwt.class);
		
		/*
		 * Vanilla Implementation binding
		 */
		
		// Node Model Creator
		bind(NodeModelCreator.class).to(NodeModelCreatorImpl.class);
		
		/*
		 * Places
		 */
		
		// The home page
		bind(HomeViewImpl.class).in(Singleton.class);
		bind(HomeView.class).to(HomeViewImpl.class);
		
		// The home page for all datasets
		bind(DatasetsHomeViewImpl.class).in(Singleton.class);
		bind(DatasetsHomeView.class).to(DatasetsHomeViewImpl.class);
		
		// DatasetView
		bind(EntityViewImpl.class).in(Singleton.class);
		bind(EntityView.class).to(EntityViewImpl.class);

		// DatasetView
		bind(DatasetViewImpl.class).in(Singleton.class);
		bind(DatasetView.class).to(DatasetViewImpl.class);

		// LayerView
		bind(LayerViewImpl.class).in(Singleton.class);
		bind(LayerView.class).to(LayerViewImpl.class);

		// ProjectsHomeView
		bind(ProjectsHomeViewImpl.class).in(Singleton.class);
		bind(ProjectsHomeView.class).to(ProjectsHomeViewImpl.class);		
		
		// ProjectView
		bind(ProjectViewImpl.class).in(Singleton.class);
		bind(ProjectView.class).to(ProjectViewImpl.class);		
		
		// AnalysesHomeView
		bind(AnalysesHomeViewImpl.class).in(Singleton.class);
		bind(AnalysesHomeView.class).to(AnalysesHomeViewImpl.class);		
		
		// AnalysisView
		bind(AnalysisViewImpl.class).in(Singleton.class);
		bind(AnalysisView.class).to(AnalysisViewImpl.class);	
		
		// StepsHomeView
		bind(StepsHomeViewImpl.class).in(Singleton.class);
		bind(StepsHomeView.class).to(StepsHomeViewImpl.class);		
		
		// StepView
		bind(StepViewImpl.class).in(Singleton.class);
		bind(StepView.class).to(StepViewImpl.class);	
		
		// QueryService View
		//bind(QueryServiceTableView.class).to(QueryServiceTableViewImpl.class);
		bind(QueryServiceTableView.class).to(QueryServiceTableViewGxtImpl.class);
		
		// LoginView
		bind(LoginViewImpl.class).in(Singleton.class);
		bind(LoginView.class).to(LoginViewImpl.class);
		
		// PasswordResetView
		bind(PasswordResetViewImpl.class).in(Singleton.class);
		bind(PasswordResetView.class).to(PasswordResetViewImpl.class);

		// RegisterAccountView
		bind(RegisterAccountViewImpl.class).in(Singleton.class);
		bind(RegisterAccountView.class).to(RegisterAccountViewImpl.class);

		// ProfileView
		bind(ProfileViewImpl.class).in(Singleton.class);
		bind(ProfileView.class).to(ProfileViewImpl.class);		
		
		// CominSoonView
		bind(ComingSoonViewImpl.class).in(Singleton.class);
		bind(ComingSoonView.class).to(ComingSoonViewImpl.class);					
		
		// PhenoEditView
		bind(PhenoEditViewImpl.class).in(Singleton.class);
		bind(PhenoEditView.class).to(PhenoEditViewImpl.class);					
		
		// LookupView
		bind(LookupViewImpl.class).in(Singleton.class);
		bind(LookupView.class).to(LookupViewImpl.class);					
		
		
		/*
		 * Widgets
		 */
		
		// LoginWidget
		bind(LoginWidgetViewImpl.class).in(Singleton.class);
		bind(LoginWidgetView.class).to(LoginWidgetViewImpl.class);
		
		// StaticTable
		bind(StaticTableView.class).to(StaticTableViewImpl.class);
		
		// LicenseBox
		bind(LicensedDownloaderView.class).to(LicensedDownloaderViewImpl.class);
		
		// Modal View
		bind(ModalWindowView.class).to(ModalWindowViewImpl.class);
		
		// Breadcrumb
		bind(BreadcrumbView.class).to(BreadcrumbViewImpl.class);
		
		// Bind the cookie provider
		bind(GWTCookieImpl.class).in(Singleton.class);
		bind(CookieProvider.class).to(GWTCookieImpl.class);

		// ColumnFactory
		bind(ColumnFactory.class).to(ColumnFactoryImpl.class);
		
		// The ImagePrototySingleton should be...well a singleton
		bind(ImagePrototypeSingleton.class).in(Singleton.class);
		
		// ClientBundle for Custom widgets
		bind(CustomWidgetImageBundle.class).in(Singleton.class);
		
		// The runtime provider
		bind(CellTableProvider.class).to(CellTableProviderImpl.class);
		
		// The column popup
		bind(ColumnsPopupViewImpl.class).in(Singleton.class);
		bind(ColumnsPopupView.class).to(ColumnsPopupViewImpl.class);
		
		// Query filter
		bind(QueryFilterViewImpl.class).in(Singleton.class);
		bind(QueryFilterView.class).to(QueryFilterViewImpl.class);
		
		// Access Menu Button
		bind(AccessMenuButtonViewImpl.class).in(Singleton.class);
		bind(AccessMenuButtonView.class).to(AccessMenuButtonViewImpl.class);

		// NodeEditor
		bind(NodeEditorViewImpl.class).in(Singleton.class);
		bind(NodeEditorView.class).to(NodeEditorViewImpl.class);

		// AnnotationEditor
		bind(AnnotationEditorViewImpl.class).in(Singleton.class);
		bind(AnnotationEditorView.class).to(AnnotationEditorViewImpl.class);

		// ACL Editor
		bind(AccessControlListEditorViewImpl.class).in(Singleton.class);
		bind(AccessControlListEditorView.class).to(AccessControlListEditorViewImpl.class);
		
		// PhenotypeEditor
		bind(PhenotypeEditorViewImpl.class).in(Singleton.class);
		bind(PhenotypeEditorView.class).to(PhenotypeEditorViewImpl.class);
		
		// Column Definition Editor
		bind(ColumnDefinitionEditorViewImpl.class).in(Singleton.class);
		bind(ColumnDefinitionEditorView.class).to(ColumnDefinitionEditorViewImpl.class);		

		// Column Mapping Editor
		bind(ColumnMappingEditorViewImpl.class).in(Singleton.class);
		bind(ColumnMappingEditorView.class).to(ColumnMappingEditorViewImpl.class);		

		// Ontology Search Panel
		bind(OntologySearchPanelViewImpl.class).in(Singleton.class);
		bind(OntologySearchPanelView.class).to(OntologySearchPanelViewImpl.class);		

		// PhenotypeMatrix
		bind(PhenotypeMatrixViewImpl.class).in(Singleton.class);
		bind(PhenotypeMatrixView.class).to(PhenotypeMatrixViewImpl.class);
		
		// EntityPageTop
		bind(EntityPageTopViewImpl.class).in(Singleton.class);
		bind(EntityPageTopView.class).to(EntityPageTopViewImpl.class);
		
		// ActionMenu
		bind(ActionMenuViewImpl.class).in(Singleton.class);
		bind(ActionMenuView.class).to(ActionMenuViewImpl.class);
	}

}