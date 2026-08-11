package kbee.importer;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.util.Assert;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.dom.Domain;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.kbee.content.model.KbeeDataSet;
import com.novamens.kbee.content.model.KbeeDataSetMember;

import kbee.api.model.ApiValue;
import kbee.api.model.ApiDataSet;
import kbee.api.model.IResultSet;
import kbee.api.service.ApiService;

@Deprecated
public class ValuesImporter extends ClassificablesImporter {
	
	private int total = 0;
	private int updated = 0;
	private int i=0;

	public ValuesImporter(ApiService server, Domain domain, LocalMatcher matcher) {
		super(server, matcher);
		setDomain(domain);
	}
	
	@Override
	public void execute() throws ContentMgmtException  {
		for (ApiDataSet remote : getRemoteDataSets()) {
			if (!"DATE".equals(remote.getType()) && !"USER".equals(remote.getType()) && !"EXTERNAL".equals(remote.getType())) {
				DataSet local = getLocal(KbeeDataSet.class, remote);
				if (local!=null && local.getClassifiers().isEmpty()) {
					importValues(remote);
				}
			}
		}
		getContentDao().flush();
		for (ApiDataSet remote : getRemoteDataSets()) {
			if (!"DATE".equals(remote.getType()) && !"USER".equals(remote.getType()) && !"EXTERNAL".equals(remote.getType())) {
				DataSet local = getLocal(KbeeDataSet.class, remote);
				if (local!=null) {
					importValues(remote);
				}
			}
		}
	}

	@Override
	public int getTotal() {
		if (total == 0) {
			for (ApiDataSet remote : getRemoteDataSets()) {
				if (!"DATE".equals(remote.getType()) && !"USER".equals(remote.getType())) {
					DataSet local = getLocal(KbeeDataSet.class, remote);
					if (local!=null) {
						IResultSet<ApiValue> values = getServer().getValues(remote);
						total += values.getSize();
					}
				}
			}
		}
		return total;
	}
	
	@Override
	public String getResult() {
		String result = String.valueOf(getTotal())+" values processed. ";
		result += String.valueOf(updated)+" values updated. ";
		return result;
	}
	
	protected void importValues(ApiDataSet dataset) throws ContentMgmtException {
		try {
			IResultSet<ApiValue> values = getServer().getValues(dataset);
			DataSet localdataset = getLocal(KbeeDataSet.class, dataset);
			Assert.isTrue(localdataset!=null, "invalid dataset!");
			while (values.hasNext()) {
				ApiValue remote = values.next();
				DataSetMember local = getLocal(KbeeDataSetMember.class, remote);
				if (local==null || remote.getLastModifiedDate().isAfter(local.getLastModifiedOffsetDateTime()) || forceUpdate()) {
					if (local == null) {
						local = createMember(localdataset);
						setLocal(remote, local);
					}	
					syncMember(remote, local);
					updated++;
					update(local);
				 	info("Value "+local.getDisplayName()+ "Updated");
				}
				else {
				 	info("Value "+local.getDisplayName());
				}
				setProgress(i);
			}
		}
		catch (Throwable e) {
			e.printStackTrace();
			logger.error(e);
			throw new ContentMgmtException(e);
		}
	}
	
	private void syncMember(ApiValue remote, DataSetMember local) {
		local.setStrValue(remote.getDisplayName());
		if (remote.getAttributes()!=null)
		syncClassifiers(remote, local, local.getDataSet().getClassifiers());
		syncAttributes(remote, local, local.getDataSet().getAttributes());
		if (remote.getParent()!=null) {
			ApiValue iparent = new ApiValue();
			iparent.setId(remote.getParent().getId());
			iparent.setDomain(remote.getDomain());
			DataSetMember parent = getLocal(KbeeDataSetMember.class, iparent);
			if (parent!=null) {
				//local.setParent(parent);
			}
		}
		local.setLastModifiedOffsetDateTime(remote.getLastModifiedDate());
	}
	
	private DataSetMember createMember(DataSet dataset) {
		DataSetMember member = dataset.createMember();
		member.setDomain(dataset.getDomain());
		member.setLastModifiedUser(dataset.getLastModifiedUser());
		member.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		update(member);
		return member;
	}

	private List<ApiDataSet> getRemoteDataSets() {
		return getServer().getDataSets();
	}
}