+ EventTypesWithCleanup {
	// almost a copy of the *cleanup method
	*cleanupEvent { | ev, flag = true |
		var type, notNode;
		type = ev[\type];
		notNode = notNodeType[type] ? true;
		if (flag || notNode) {
			^ (	parent: ev,
				type: cleanupTypes[type]
			);
		}
	}
}
