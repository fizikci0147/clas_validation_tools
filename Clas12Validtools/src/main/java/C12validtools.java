/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author fizikci0147
 */
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;

import com.sun.prism.Graphics;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;

import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.math.F1D;

import org.jlab.clas.physics.Particle;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.groot.group.DataGroup;
import org.jlab.utils.groups.IndexedList;

public class C12validtools {

    int nEvents = 0;
    private DataBank recBank;
    private DataBank runBank;
    IndexedList<DataGroup> dataGroups      = new IndexedList<DataGroup>(1);
    EmbeddedCanvasTabbed   canvasTabbed    = null;
    ArrayList<String>      canvasTabNames  = new ArrayList<String>();


    public static void main(String[] args){
        C12validtools ttest = new C12validtools();

        ttest.setAnalysisTabNames("TBT Positive Tracks");

        ttest.CreateHistos();

        String fileName="/Users/fizikci0147/work/clas_work/clas_validation_tools/small.hipo";
        //String fileName="/Users/michaelnycz/JLAB_Programs/clas_validation_tools/small.hipo";
        File file = new File(fileName);
        if (!file.exists() || file.isDirectory()) {
            System.err.println("Cannot find input file.");
        }

        HipoDataSource reader = new HipoDataSource();
        reader.open(fileName);

        while (reader.hasEvent()) {
            DataEvent event = reader.getNextEvent();
            ttest.ProcessEvent(event);
        }
        reader.close();
        JFrame frame = new JFrame("C12Validtools");
        frame.setSize(1200, 800);
        frame.add(ttest.canvasTabbed);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        ttest.plotHistos();

    }

    private void plotHistos() {
        /* plotting ....*/
        canvasTabbed.getCanvas("TBT Positive Tracks").divide(2,2);
        canvasTabbed.getCanvas("TBT Positive Tracks").setGridX(false);
        canvasTabbed.getCanvas("TBT Positive Tracks").setGridY(false);
        canvasTabbed.getCanvas("TBT Positive Tracks").cd(0);
        canvasTabbed.getCanvas("TBT Positive Tracks").draw(dataGroups.getItem(1).getH1F("hi_p_pos"));
        canvasTabbed.getCanvas("TBT Positive Tracks").cd(1);
        canvasTabbed.getCanvas("TBT Positive Tracks").draw(dataGroups.getItem(1).getH1F("h_px"));
        canvasTabbed.getCanvas("TBT Positive Tracks").cd(2);
        canvasTabbed.getCanvas("TBT Positive Tracks").draw(dataGroups.getItem(1).getH1F("hvert_t"));
    }
    /*creating Histos*/
    private void CreateHistos() {
        H1F hi_p_pos = new H1F("hi_p_pos", "hi_p_pos", 100, 0.0, 8.0);
        hi_p_pos.setTitleX("p (GeV)");
        hi_p_pos.setTitleY("Counts");
        H1F hvert_x = new H1F("h_px", "h_px", 100, -0.1, 0.1);
        hvert_x.setTitleX("Px");
        hvert_x.setTitleY("Counts");
        H1F hvert_t = new H1F("hvert_t","hvert_t",500,-100,400);
        hvert_t.setTitleX("Vt");
        hvert_t.setTitleY("Counts");
        H1F beta = new H1F("Beta","Beta",100,-1,3);
        beta.setTitleX("Beta");
        beta.setTitleY("Counts");
        DataGroup dg_pos = new DataGroup(1,1);
        dg_pos.addDataSet(hi_p_pos, 1);
        dg_pos.addDataSet(hvert_x, 2);
        dg_pos.addDataSet(hvert_t,3);
        dg_pos.addDataSet(beta,4);
        dataGroups.add(dg_pos, 1);

    }
    public void setAnalysisTabNames(String... names) {
        for(String name : names) {
            canvasTabNames.add(name);
        }
        canvasTabbed = new EmbeddedCanvasTabbed(names);
    }

    private DataBank getBank(DataEvent de,String bankName) {
        DataBank bank=null;
        if (de.hasBank(bankName)) {
            bank=de.getBank(bankName);
        }
        return bank;
    }

    private void getBanks(DataEvent de) {
        recBank     = getBank(de,"REC::Event");
        runBank     = getBank(de,"RUN::config");
        loadMaps();
    }

    public void loadMap(Map<Integer,List<Integer>> map, 
            DataBank fromBank, 
            DataBank toBank, 
            String idxVarName) {
        map.clear();
        if (fromBank==null) return;
        if (toBank==null) return;
        for (int ii=0; ii<fromBank.rows(); ii++) {
            final int iTo=fromBank.getInt(idxVarName,ii);
            if (map.containsKey(iTo)) {
                map.get(iTo).add(ii);
            }
            else {
                List<Integer> iFrom=new ArrayList<Integer>();
                map.put(iTo,iFrom);
                map.get(iTo).add(ii);
            }
        }
    }

    /**
     *
     * Load mapping from REC::Particle to REC::"Detector".
     *
     */
    public void loadMaps() {
        //loadMap(recCalMap,recCalBank,recPartBank,"pindex");
    }


    public DataBank getDetectorBank(int detId) {
        DataBank bankTo=null;
        return null;
    }


    private void ProcessEvent(DataEvent event) {
        nEvents++;
        if ((nEvents % 10000) == 0) System.out.println("Analyzed " + nEvents + " events");

       if(event.hasBank("REC::Particle")==true) {
            DataBank bank = event.getBank("REC::Particle");
            int rows = bank.rows();
            for (int loop = 0; loop < rows; loop++) {
                int pidCode = 0;
                if (bank.getByte("q", loop) == -1) pidCode = 11;
                else if (bank.getByte("q", loop) == 1) pidCode = 211;
                else pidCode = 22;

                Particle recParticle = new Particle(
                        pidCode,
                        bank.getFloat("px", loop),
                        bank.getFloat("py", loop),
                        bank.getFloat("pz", loop),
                        bank.getFloat("Vx", loop),
                        bank.getFloat("Vy", loop),
                        bank.getFloat("Vz", loop));
                float vert_t = bank.getFloat("Vt", loop);

                System.out.println(recParticle.charge());
                dataGroups.getItem(1).getH1F("hi_p_pos").fill(recParticle.p());
                dataGroups.getItem(1).getH1F("h_px").fill(recParticle.px());
                dataGroups.getItem(1).getH1F("hvert_t").fill(vert_t);


            }
        }

    }


}