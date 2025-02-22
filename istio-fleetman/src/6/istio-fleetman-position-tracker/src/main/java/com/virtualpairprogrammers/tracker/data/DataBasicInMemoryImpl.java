package com.virtualpairprogrammers.tracker.data;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import com.virtualpairprogrammers.tracker.domain.VehicleBuilder;
import com.virtualpairprogrammers.tracker.domain.VehicleNotFoundException;
import com.virtualpairprogrammers.tracker.domain.VehiclePosition;
import com.virtualpairprogrammers.tracker.externalservices.ExternalVehicleTelemetryService;
import com.virtualpairprogrammers.tracker.externalservices.DriverMonitoringService;
import com.virtualpairprogrammers.tracker.externalservices.TelemetryServiceUnavailableException;

/**
 * This is a quick and dirty dev-standin (for local testing) that stores vehicle position reports
 * in a memory structure. 
 * 
 * Thread safety is NOT OFFERED; remember this is intended to be used in local development.
 * 
 * Use the database backed implementation in a production, clustered or cloud environment.
 */
@Repository
@Profile({"production-microservice","standalone"})
public class DataBasicInMemoryImpl implements Data 
{
	private Map<String,TreeSet<VehiclePosition>> positionDatabase;

	@Autowired
	private ExternalVehicleTelemetryService telemetryService;

	// See case #22, bad code but we were forced to do this!
	@Autowired
	private DriverMonitoringService driverMonitoringService;

	public DataBasicInMemoryImpl()
	{
		positionDatabase = new HashMap<>();
	}

	@Override
	public void updatePosition(VehiclePosition data)
	{
		String vehicleName = data.getName();
		TreeSet<VehiclePosition> positions = positionDatabase.get(vehicleName);
		if (positions == null) 
		{
			positions = new TreeSet<>();
			positionDatabase.put(vehicleName, positions);
		}

		VehiclePosition vehicleWithSpeed;
		try {
			BigDecimal speed = telemetryService.getSpeedFor(vehicleName);
			vehicleWithSpeed = new VehicleBuilder().withVehiclePostion(data).withSpeed(speed).withTimestamp(new Date()).build();
		} catch (TelemetryServiceUnavailableException e) {
			vehicleWithSpeed = new VehicleBuilder().withVehiclePostion(data).withTimestamp(new Date()).build();
		}
		positions.add(vehicleWithSpeed);
		telemetryService.updateData(data); // see case #8 for details on why we do this last

		String speed = (vehicleWithSpeed.getSpeed() != null) ? ""+ vehicleWithSpeed.getSpeed() : "0";

		driverMonitoringService.updateSpeedDataFor(vehicleWithSpeed.getName(), speed);
	}

	@Override
	public VehiclePosition getLatestPositionFor(String vehicleName) throws VehicleNotFoundException
	{
		TreeSet<VehiclePosition> reportsForThisVehicle = positionDatabase.get(vehicleName);
		if (reportsForThisVehicle == null) throw new VehicleNotFoundException();
		return reportsForThisVehicle.first();
	}

	@Override
	public void addAllReports(VehiclePosition[] allReports) {
		for (VehiclePosition next: allReports)
		{
			this.updatePosition(next);
		}
	}

	@Override
	public Set<VehiclePosition> getLatestPositionsOfAllVehicles() {
		Set<VehiclePosition> results = new HashSet<>();

		for (TreeSet<VehiclePosition> reports: this.positionDatabase.values())
		{
			if (!reports.isEmpty()) results.add(reports.first());
		}
		return results;
	}

	public Collection<VehiclePosition> getHistoryFor(String vehicleName) throws VehicleNotFoundException 
	{
        return this.positionDatabase.get(vehicleName);
	}

	public void reset() {
		positionDatabase.clear();
	}

	public void setTelemetryService(ExternalVehicleTelemetryService ts) {
		this.telemetryService = ts;
	}
}
