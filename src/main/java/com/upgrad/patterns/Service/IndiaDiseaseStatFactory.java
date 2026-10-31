package com.upgrad.patterns.Service;

import com.upgrad.patterns.Interfaces.IndianDiseaseStat;
import com.upgrad.patterns.Constants.SourceType;
import com.upgrad.patterns.Strategies.DiseaseShStrategy;
import com.upgrad.patterns.Strategies.JohnHopkinsStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IndiaDiseaseStatFactory {
    private IndianDiseaseStat diseaseShStrategy;
    private IndianDiseaseStat johnHopkinsStrategy;

    @Autowired
    public IndiaDiseaseStatFactory(DiseaseShStrategy diseaseShStrategy, JohnHopkinsStrategy johnHopkinsStrategy)
    {
        this.diseaseShStrategy = diseaseShStrategy;
        this.johnHopkinsStrategy = johnHopkinsStrategy;
    }

    
    public IndianDiseaseStat GetInstance(SourceType sourceType) {
        if (sourceType == SourceType.JohnHopkins) {
            return johnHopkinsStrategy;
        } else if (sourceType == SourceType.DiseaseSh) {
            return diseaseShStrategy;
        }
        String message = String.format("invalid disease strategy/sourceType (%s, %s)", SourceType.DiseaseSh, SourceType.JohnHopkins);
        throw new IllegalArgumentException(message);
    }
}
