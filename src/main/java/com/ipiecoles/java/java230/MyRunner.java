package com.ipiecoles.java.java230;

import com.ipiecoles.java.java230.exceptions.BatchException;
import com.ipiecoles.java.java230.model.Commercial;
import com.ipiecoles.java.java230.model.Employe;
import com.ipiecoles.java.java230.model.Manager;
import com.ipiecoles.java.java230.model.Technicien;
import com.ipiecoles.java.java230.repository.EmployeRepository;
import com.ipiecoles.java.java230.repository.ManagerRepository;
import jdk.vm.ci.meta.Local;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class MyRunner implements CommandLineRunner {

    private static final String REGEX_MATRICULE = "^[MTC][0-9]{5}$";
    private static final String REGEX_NOM = "^[\\p{L}- ]*$";
    private static final String REGEX_PRENOM = "^[\\p{L}- ]*$";
    private static final String REGEX_SALAIRE = "[0-9]*.[0-9]";
    private static final String REGEX_INT = "[0-9]*";
    private static final String REGEX_GRADE = "[1-5]";
    private static final int NB_CHAMPS_MANAGER = 5;
    private static final int NB_CHAMPS_TECHNICIEN = 7;
    private static final String REGEX_MATRICULE_MANAGER = "^M[0-9]{5}$";
    private static final int NB_CHAMPS_COMMERCIAL = 7;

    @Autowired
    private EmployeRepository employeRepository;

    @Autowired
    private ManagerRepository managerRepository;

    private List<Employe> employes = new ArrayList<>();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void run(String... strings) throws Exception {
        String fileName = "employes.csv";
        readFile(fileName);
        //readFile(strings[0]);
    }

    /**
     * Méthode qui lit le fichier CSV en paramètre afin d'intégrer son contenu en BDD
     * @param fileName Le nom du fichier (à mettre dans src/main/resources)
     * @return une liste contenant les employés à insérer en BDD ou null si le fichier n'a pas pu être le
     */
    public List<Employe> readFile(String fileName)  {
        Stream<String> stream;
        logger.info("Lecture du ficher : " + fileName);

        try {
            stream = Files.lines(Paths.get(new ClassPathResource(fileName).getURI()));
        } catch (IOException e) {
            logger.error("Problème dans l'ouverture du fichier " + fileName);
            return new ArrayList<>();
        }
        //logger.info(stream.count() + "lignes lues");
        List<String> ligne = stream.collect(Collectors.toList());
        logger.info(ligne.size() + "lignes lues");

        for(int i = 0; i < ligne.size(); i++){
            try {
                processLine(ligne.get(i));
            } catch (BatchException e) {
                //??
                logger.error("Ligne " + (i+1) + " : " + e.getMessage()+ " => " + ligne.get(i));
            }

        }
        //TODO

        return employes;
    }

    /**
     * Méthode qui regarde le premier caractère de la ligne et appelle la bonne méthode de création d'employé
     * @param ligne la ligne à analyser
     * @throws BatchException si le type d'employé n'a pas été reconnu
     */
    private void processLine(String ligne) throws BatchException {
        //TODO
        switch (ligne.substring(0,1)){
            case "T":
                processTechnicien(ligne);
                break;
            case "M":
                processManager(ligne);
                break;
            case "C":
                processCommercial(ligne);
                break;
            default:
                throw new BatchException("Type d'emplyé inconnu : ");
        };
    }

    /**
     * Méthode qui crée un Commercial à partir d'une ligne contenant les informations d'un commercial et l'ajoute dans la liste globale des employés
     * @param ligneCommercial la ligne contenant les infos du commercial à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processCommercial(String ligneCommercial) throws BatchException {
        //TODO
        String[] commercialFields = ligneCommercial.split(",");

        if(!(commercialFields.length == NB_CHAMPS_COMMERCIAL)){
            throw new BatchException(": le commercial ne comprend pas le bon nombre de champs ");
        }
        if(!commercialFields[0].matches(REGEX_MATRICULE)) {
            throw new BatchException(": le matricule ne respecte pas l'expression régulière ^[MTC][0-9]{5}$ ");
        }
        if(!commercialFields[1].matches(REGEX_NOM)){
            throw new BatchException(": la chaîne C12 ne respecte pas l'expression régulière ^[\\p{L}- ]*$  ");
        }
        if(!commercialFields[2].matches(REGEX_PRENOM)){
            throw new BatchException(": la chaîne C12 ne respecte pas l'expression régulière ^[\\p{L}- ]*$ ");
        }
        LocalDate date;
        try {
            date = DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(commercialFields[3]);
        } catch (Exception e) {
            throw new BatchException(": la chaîne C12 ne respecte pas le format de date dd/MM/yyyy");
        }
        if(!commercialFields[4].matches(REGEX_SALAIRE)){
            throw new BatchException(": la chaîne C12 ne respecte pas l'expression régulière [0-9]*.[0-9] ");

        }
        Double salaire = Double.parseDouble(commercialFields[4]);
        if(!commercialFields[5].matches(REGEX_INT)){
            throw new BatchException(": Le chiffre d'affaire du commercial est incorrect ");
        }
        Double ca = Double.parseDouble(commercialFields[4]);
        if(!commercialFields[5].matches(REGEX_INT)){
            throw new BatchException(": La performance du commercial est incorrect ");
        }
        Integer performance = Integer.parseInt(commercialFields[4]);
        Commercial c = new Commercial();
        c.setMatricule(commercialFields[0]);
        c.setNom(commercialFields[1]);
        c.setPrenom(commercialFields[2]);
        c.setDateEmbauche(date);
        c.setSalaire(salaire);
        c.setCaAnnuel(ca);
        c.setPerformance(performance);
        employes.add(c);

    }




    /**
     * Méthode qui crée un Manager à partir d'une ligne contenant les informations d'un manager et l'ajoute dans la liste globale des employés
     * @param ligneManager la ligne contenant les infos du manager à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processManager(String ligneManager) throws BatchException {
        //TODO

        String[] managerFields = ligneManager.split(",");

        if(!(managerFields.length == NB_CHAMPS_MANAGER)){
            throw new BatchException(": le manager ne comprend pas le bon nombre de champs ");
        }
        if(!managerFields[0].matches(REGEX_MATRICULE)) {
            throw new BatchException(": Le matricule ne respecte pas l'expression régulière ^[MTC][0-9]{5}$ ");
        }
        if(!managerFields[1].matches(REGEX_NOM)){
            throw new BatchException(": La chaîne C12 ne respecte pas l'expression régulière ^[\\p{L}- ]*$  ");
        }
        if(!managerFields[2].matches(REGEX_PRENOM)){
            throw new BatchException(": La chaîne C12 ne respecte pas l'expression régulière ^[\\p{L}- ]*$ ");
        }
        LocalDate date;
        try {
            date = DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(managerFields[3]);
        } catch (Exception e) {
            throw new BatchException(": La chaîne C12 ne respecte pas le format de date dd/MM/yyyy");
        }
        if(!managerFields[4].matches(REGEX_SALAIRE)){
            throw new BatchException(": La chaîne C12 ne respecte pas l'expression régulière [0-9]*.[0-9] ");
        }
        Double salaire = Double.parseDouble(managerFields[4]);

        Manager c = new Manager();
        c.setMatricule(managerFields[0]);
        c.setNom(managerFields[1]);
        c.setPrenom(managerFields[2]);
        c.setDateEmbauche(date);
        c.setSalaire(salaire);
        employes.add(c);
    }

    /**
     * Méthode qui crée un Technicien à partir d'une ligne contenant les informations d'un technicien et l'ajoute dans la liste globale des employés
     * @param ligneTechnicien la ligne contenant les infos du technicien à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processTechnicien(String ligneTechnicien) throws BatchException {

        /*if(!commercialFields[5].matches(REGEX_GRADE_INT)){
            throw new BatchException(": le grade n'est pas un chiffre ");
        }
        if(!commercialFields[5].matches(REGEX_GRADE)){
            throw new BatchException(": le grade n'est pas compris entre 1 et 5 ");
        }*/
        //TODO
        String[] technicienFields = ligneTechnicien.split(",");

        if(!(technicienFields.length == NB_CHAMPS_TECHNICIEN)){
            throw new BatchException(": le technicien ne comprend pas le bon nombre de champs ");
        }
        if(!technicienFields[0].matches(REGEX_MATRICULE)) {
            throw new BatchException(": Le matricule ne respecte pas l'expression régulière ^[MTC][0-9]{5}$ ");
        }
        if(!technicienFields[1].matches(REGEX_NOM)){
            throw new BatchException(": La chaîne C12 ne respecte pas l'expression régulière ^[\\p{L}- ]*$  ");
        }
        if(!technicienFields[2].matches(REGEX_PRENOM)){
            throw new BatchException(": La chaîne C12 ne respecte pas l'expression régulière ^[\\p{L}- ]*$ ");
        }
        LocalDate date;
        try {
            date = DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(technicienFields[3]);
        } catch (Exception e) {
            throw new BatchException(": La chaîne C12 ne respecte pas le format de date dd/MM/yyyy");
        }
        if(!technicienFields[4].matches(REGEX_SALAIRE)){
            throw new BatchException(": La chaîne C12 ne respecte pas l'expression régulière [0-9]*.[0-9] ");
        }
        Double salaire = Double.parseDouble(technicienFields[4])
        if(!technicienFields[5].matches(REGEX_INT)){
            throw new BatchException(": Le chiffre d'affaire du technicien est incorrect ");
        }
        if(!technicienFields[5].matches(REGEX_GRADE)){
            throw new BatchException(": Le grade n'est pas compris entre 1 et 5 ");
        }
        Integer grade = Integer.parseInt(technicienFields[5]);
        if(!technicienFields[6].matches(REGEX_MATRICULE_MANAGER)){
            throw new BatchException(": Le technicien n'a pas de manager");
        }


        Technicien c = new Technicien();
        c.setMatricule(technicienFields[0]);
        c.setNom(technicienFields[1]);
        c.setPrenom(technicienFields[2]);
        c.setDateEmbauche(date);
        c.setSalaire(salaire);
        c.setGrade(grade);

        employes.add(c);
    }

}
