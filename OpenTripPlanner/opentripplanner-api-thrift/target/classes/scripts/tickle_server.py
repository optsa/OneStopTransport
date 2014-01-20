#!/usr/bin/python

from opentripplanner.api.thrift.definition import OTPService
from opentripplanner.api.thrift.definition.location.ttypes import Location
from opentripplanner.api.thrift.definition.query.ttypes import NearestEdgesQuery
from opentripplanner.api.thrift.definition.query.ttypes import VertexQuery
from opentripplanner.api.thrift.definition.trip.ttypes import PathOptions
from opentripplanner.api.thrift.definition.trip.ttypes import TravelMode
from opentripplanner.api.thrift.definition.trip.ttypes import TripParameters
from optparse import OptionParser
from thrift import Thrift
from thrift.protocol import TBinaryProtocol
from thrift.transport import TSocket
from thrift.transport import TTransport

import random
import time


def Connect(host, port):
    try:
        # Make socket
        transport = TSocket.TSocket(host, port=port)
            
        # Buffering is critical. Raw sockets are very slow
        transport = TTransport.TBufferedTransport(transport)
        
        # Wrap in a protocol
        protocol = TBinaryProtocol.TBinaryProtocolAccelerated(transport)
        
        # Create a client to use the protocol encoder
        client = OTPService.Client(protocol)
        
        # Connect!
        transport.open()
        
        return client
    except Thrift.TException, tx:
        print "%s" % (tx.message)


def Main():
    usage = ("usage: python lt_pings.py -p port")
    parser = OptionParser(usage=usage)
    parser.add_option("-H", "--host",
                      dest="host", default="localhost",
                      help="host where server lives")
    parser.add_option("-p", "--port",
                      dest="port", type="int", default=30303,
                      help="port at which server lives")
    options, unused_args = parser.parse_args()
    assert options.port

    client = Connect(options.host, options.port)
    assert client, 'Failed to connect'
    
    req = OTPService.GraphEdgesRequest()
    start_t = time.time()
    res = client.GetEdges(req)
    total_t = time.time() - start_t    

    edges = res.edges
    reported_compute_time = float(res.compute_time_millis) / 1000.0
    print 'GraphEdgesRequest took %.6f seconds' % total_t 
    print 'Server computation time %.6f seconds' % reported_compute_time
    print '\tReturned %d edges' % len(edges)    
    
    req = OTPService.GraphVerticesRequest()
    start_t = time.time()
    res = client.GetVertices(req)
    total_t = time.time() - start_t
    
    vertices = res.vertices
    reported_compute_time = float(res.compute_time_millis) / 1000.0
    print 'GraphVerticesRequest took %.6f seconds' % total_t 
    print 'Server computation time %.6f seconds' % reported_compute_time
    print '\tReturned %d vertices' % len(vertices)
    
    # Sample an origin and a destination (deterministically)
    random.seed(123456)
    origin, dest = random.sample(vertices, 2)
    origin_ll = origin.lat_lng
    dest_ll = dest.lat_lng
    origin_loc = Location(lat_lng=origin_ll)
    dest_loc = Location(lat_lng=dest_ll)
    
    # Run a geocoding request
    req = OTPService.FindNearestVertexRequest(
        query=VertexQuery(location=origin_loc))
    start_t = time.time()
    res = client.FindNearestVertex(req) 
    total_t = time.time() - start_t
    
    result = res.result
    reported_compute_time = float(res.compute_time_millis) / 1000.0
    print 'FindNearestVertexRequest took %.6f seconds' % total_t
    print 'Server computation time %.6f seconds' % reported_compute_time
    print 'Nearest vertex: ', result.nearest_vertex
    
    # Run a bulk vertex search geocoding request
    req = OTPService.BulkFindNearestVertexRequest(
        queries=[VertexQuery(location=origin_loc),
               VertexQuery(location=dest_loc)])
    start_t = time.time()
    res = client.BulkFindNearestVertex(req) 
    total_t = time.time() - start_t
    
    results = res.results
    reported_compute_time = float(res.compute_time_millis) / 1000.0
    print 'BulkFindNearestVertexRequest took %.6f seconds' % total_t
    print 'Server computation time %.6f seconds' % reported_compute_time
    print 'Results: ', results
    
    q = NearestEdgesQuery(location=origin_loc,
                          allowed_modes=set([TravelMode.CAR]))
    req = OTPService.FindNearestEdgesRequest(query=q)
    start_t = time.time()
    res = client.FindNearestEdges(req)
    total_t = time.time() - start_t
    
    result = res.result
    reported_compute_time = float(res.compute_time_millis) / 1000.0
    print 'FindNearestEdgesRequest took %.6f seconds' % total_t
    print 'Server computation time %.6f seconds' % reported_compute_time
    print 'Nearest edges: ', result.nearest_edges
    
    q2 = NearestEdgesQuery(location=dest_loc,
                           allowed_modes=set([TravelMode.CAR]))
    req = OTPService.BulkFindNearestEdgesRequest(queries=[q, q2])
    start_t = time.time()
    res = client.BulkFindNearestEdges(req)
    total_t = time.time() - start_t
    
    results = res.results
    reported_compute_time = float(res.compute_time_millis) / 1000.0
    print 'BulkFindNearestEdgesRequest took %.6f seconds' % total_t
    print 'Server computation time %.6f seconds' % reported_compute_time
    print 'Results: ', results
    
    # Request a walking trip between them.
    trip_params = TripParameters(
        origin=origin_loc, destination=dest_loc,
        allowed_modes=set([TravelMode.WALK]))    
    path_opts = PathOptions(num_paths=1, return_detailed_path=True)
    req = OTPService.FindPathsRequest(trip=trip_params,
                                      options=path_opts)
    start_t = time.time()
    res = client.FindPaths(req)
    total_t = time.time() - start_t

    reported_compute_time = float(res.compute_time_millis) / 1000.0
    print 'FindPathsRequest took %.6f seconds' % total_t
    print 'Server computation time %.6f seconds' % reported_compute_time
    paths = res.paths
    print paths
    if paths.no_paths_found:
        print 'Found no paths'
    else:
        expected_duration = paths.paths[0].duration
        print 'Found path', res.paths[0]
        print 'Trip expected to take %d seconds' % expected_duration
    
    # Sample 10 origins and a destinations (deterministically)
    random.seed(12345)
    origins = random.sample(vertices, 10)
    dests = random.sample(vertices, 10)
    
    trip_params = []
    for origin, dest in zip(origins, dests):
        origin_loc = Location(lat_lng=origin.lat_lng)
        dest_loc = Location(lat_lng=dest.lat_lng)
        trip_params.append(TripParameters(
            origin=origin_loc, destination=dest_loc,
            allowed_modes=set([TravelMode.WALK])))

    path_opts = PathOptions(num_paths=1, return_detailed_path=False)
    req = OTPService.BulkPathsRequest(trips=trip_params,
                                      options=path_opts)
    
    try:
        start_t = time.time()
        res = client.BulkFindPaths(req)
        total_t = time.time() - start_t

        reported_compute_time = float(res.compute_time_millis) / 1000.0
        print ('BulkFindPaths took %.6f seconds '
               'for %d trips ' % (total_t, len(origins))) 
        print 'Server computation time %.6f seconds' % reported_compute_time        
    except OTPService.NoPathFoundError, e:
        print e
        
        
if __name__ == '__main__':
    Main()
